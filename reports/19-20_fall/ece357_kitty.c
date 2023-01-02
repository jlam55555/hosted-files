#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <unistd.h>

#define BUF_SIZE 4096
#define MAX_PAR_WRITE_RETRY 256

int main(int argc, char **argv) {
  int ofd = -1, ifd = -1, flen, rlen, wlen, is_bin, rcnt, wcnt, par_retry_count;
  char buf[BUF_SIZE], *bufp, *fnames[argc+1], **fnamep, *out_file = NULL,
       *errop, *errctx, *errctm;
  memset(fnames, 0, (argc+1)*sizeof(char *));

  // parse args; only handles -o argument (handles others as filenames)
  for(fnamep = fnames, ++argv; --argc; ++argv)
    if(!strcmp(*argv, "-o")) {
      if(!--argc) {
        errop = "parsing", errctx = "args", errctm = "No output file after -o";
        goto fail;
      }
      out_file = *++argv;
    } else
      *fnamep++ = *argv;

  // open out_file using creat (open w/ flags O_CREAT|O_WRONLY|O_TRUNC)
  if(out_file) {
    if((ofd = creat(out_file, 0666)) == -1) {
      errop = "creating (for writing)", errctx = out_file;
      goto fail;
    }
  } else
    ofd = 1;

  // if no input file specified (fnames empty), add std. input to input list
  if(!*fnames)
    *fnames = "-";

  // loop through and open input files, concatenate to output
  for(fnamep = fnames; *fnamep; fnamep++) {
    if(strcmp(*fnamep, "-")) {
      if((ifd = open(*fnamep, O_RDONLY)) == -1) {
        errop = "opening (for reading)", errctx = *fnamep;
        goto fail;
      }
    } else
      ifd = 0;

    // attempt reading file
    flen = is_bin = rcnt = wcnt = 0;
    while(rcnt++, rlen = read(ifd, buf, BUF_SIZE)) {
      if(rlen == -1) {
        errop = "reading of", errctx = *fnamep;
        goto fail;
      }

      // write to output file
      // account for partial write scenario; most likely due to a pipe/socket
      // with a small buffer; keep retrying until exceeded maximum tries or
      // write complete; while loop breaks when buffer successfully written
      wlen = 0, par_retry_count = 0;
      while(wcnt++, (wlen += write(ofd, buf+wlen, rlen-wlen)) != rlen) {
        if(++par_retry_count == MAX_PAR_WRITE_RETRY) {
          errop = "writing to", errctx = out_file, errctm = "Partial write";
          goto fail;
        }

        // write error
        if(wlen == -1) {
          errop = "writing to", errctx = out_file;
          goto fail;
        }
      }

      // add to total length (in bytes) and read/write count
      flen += rlen;

      // check if file includes binary chars
      if(!is_bin)
        for(bufp = buf; bufp-buf < rlen; bufp++)
          if((*bufp < 32 || *bufp >= 127) && !(*bufp >= 9 && *bufp <= 13)) {
            is_bin = 1;
            break;
          }
    }

    // close input file
    if(ifd > 2 && close(ifd) == -1) {
      errop = "closing", errctx = *fnamep;
      goto fail;
    }

    // report bytes transferred for file to stderr
    fprintf(stderr, "%s%s: %d bytes transferred. %d read / %d write call(s).\n",
            ifd ? *fnamep : "<standard input>", is_bin ? " [BINARY]" : "",
            flen, rcnt, wcnt);
  }

  // close output file and exit
  if(ofd > 2 && close(ofd) == -1) {
    errop = "closing", errctx = out_file, ofd = -1;
    goto fail;
  }
  return 0;

fail:
  fprintf(stderr, "Error: %s %s: %s\n",
          errop, errctx, errno ? strerror(errno) : errctm);

  // attempt to close input/output files
  // silently fail here because files will automatically be closed anyway
  // and to avoid extra errors printed to screen
  if(ofd != -1)
    close(ofd);
  if(ifd != -1)
    close(ifd);
  return -1;
}
