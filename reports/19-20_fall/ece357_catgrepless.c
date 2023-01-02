#include <errno.h>
#include <fcntl.h>
#include <setjmp.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

#define BUF_SIZE 4096

#define ERR_FAT(prog, op, ctx, msg) {\
    dprintf(2, "%s: ERROR: %s \"%s\": %s\n", prog, op, ctx, msg);\
    exit(EXIT_FAILURE);\
  }

// handle broken pipe (SIGPIPE) in grep process (when more exits first);
// when encountered, write debug data and end this process
jmp_buf jmp_env;
void sigpipe_hand(int sig) { siglongjmp(jmp_env, 1); }
void sigint_hand(int sig) { siglongjmp(jmp_env, 2); }

int main(int argc, char **argv) {

  char buf[BUF_SIZE], *pattern, **infile, **gargv, **margv;
  int gpid, mpid, wstatus, ftg[2], gtm[2], // file-to-grep, grep-to-more pipes
      rlen, wlen, wtot, ifd, ofd, bp, jstatus; // jump status
  
  // assume input of "catgrepmore pattern infile1 [...infile2...]"
  if(argc < 3)
    ERR_FAT(argv[0], "parsing arguments", "[too few arguments]",
            "Usage:\n\tcatgrepmore pattern infile1 [...infile2...]");
  pattern = argv[1];
  infile = argv+2;

  // sets up args for execing children
  // actually uses less instead of more
  gargv = (char *[3]) {"grep", pattern, NULL};
  margv = (char *[2]) {"less", NULL};

  // initialize bytes/files processed, ifd/ofd to -1 to indicate not open
  bp = 0, ifd = ofd = -1;

  // handle SIGPIPE, SIGINT: should only be called after loop below begins
  // SIGPIPE may occur if more exists before grep has finished writing; for
  // this, simply continue to the next file. For SIGINT, do the same but also
  // print out number of bytes/files processed. Also make sure all ifd/ofd
  // are closed (if opened).
  if((jstatus = sigsetjmp(jmp_env, 1)) != 0) {
    // make sure children are dead; this is not necessary for more, but less
    // persists for SIGINT
    if(kill(mpid, SIGTERM) < 0)
      ERR_FAT(argv[0], "kill: more after signal", *infile, strerror(errno));
    waitpid(mpid, &wstatus, 0);
    waitpid(gpid, &wstatus, 0);

    // make sure fds closed (if opened)
    if(ifd != -1 && close(ifd) < 0)
      ERR_FAT(argv[0], "close: input file", *infile, strerror(errno));
    if(ofd != -1 && close(ofd) < 0)
      ERR_FAT(argv[0], "close: output pipe from parent for input file",
              *infile, strerror(errno));

    // print message if Ctrl+C
    if(jstatus == 2)
      dprintf(2, "Warning: SIGINT encountered. %d bytes/%d files processed.\n",
              bp, infile-argv-1);
    infile++;
  }
  signal(SIGINT, sigint_hand);
  signal(SIGPIPE, sigpipe_hand);

  for(; *infile; infile++) {

    // pipe 1 is from this program to grep; pipe 2 is from grep to more
    // pipe 2 is created first because to involve fewer file closings
    if(pipe(gtm) < 0)
      ERR_FAT(argv[0], "pipe: creating pipe 2", *infile, strerror(errno));
    switch(mpid = fork()) {
      case -1:
        ERR_FAT(argv[0], "fork: to more child", *infile, strerror(errno));
        break;
      case 0:
        signal(SIGINT, SIG_DFL);
        signal(SIGPIPE, SIG_DFL);

        if(dup2(gtm[0], 0) < 0)
          ERR_FAT(argv[0], "dup2: pipe 2 read to more child in", *infile,
                  strerror(errno));
        if(close(gtm[0]) < 0)
          ERR_FAT(argv[0], "close: pipe 2 read in more child", *infile,
                  strerror(errno));
        
        // this is not used, close
        if(close(gtm[1]) < 0)
          ERR_FAT(argv[0], "close: pipe 2 write in more child", *infile,
                  strerror(errno));

        if(execvp(*margv, margv) < 0)
          ERR_FAT(argv[0], "execvp: more", *infile, strerror(errno));
      default:
        if(close(gtm[0]) < 0)
          ERR_FAT(argv[0], "close: pipe 2 read in parent", *infile,
                  strerror(errno));
    }

    if(pipe(ftg) < 0)
      ERR_FAT(argv[0], "pipe: creating pipe 1", *infile, strerror(errno));
    ofd = ftg[1];
    switch(gpid = fork()) {
      case -1:
        ERR_FAT(argv[0], "fork: to grep child", *infile, strerror(errno));
        break;
      case 0:
        signal(SIGINT, SIG_DFL);
        signal(SIGPIPE, SIG_DFL);

        if(dup2(ftg[0], 0) < 0)
          ERR_FAT(argv[0], "dup2: pipe 1 read to grep child in", *infile,
                  strerror(errno));
        if(dup2(gtm[1], 1) < 0)
          ERR_FAT(argv[0], "dup2: pipe 2 write to grep child out", *infile,
                  strerror(errno));

        if(close(ftg[0]) < 0)
          ERR_FAT(argv[0], "close: pipe 1 read in grep child", *infile,
                  strerror(errno));
        if(close(ftg[1]) < 0)
          ERR_FAT(argv[0], "close: pipe 1 write in grep child", *infile,
                  strerror(errno));
        if(close(gtm[1]) < 0)
          ERR_FAT(argv[0], "close: pipe 2 write in grep child", *infile,
                  strerror(errno));

        if(execvp(*gargv, gargv) < 0)
          ERR_FAT(argv[0], "execvp: grep", pattern, strerror(errno));
      default:
        if(close(ftg[0]) < 0)
          ERR_FAT(argv[0], "close: pipe 1 read in parent", *infile,
                  strerror(errno));
        if(close(gtm[1]) < 0)
          ERR_FAT(argv[0], "close: pipe 2 write in parent", *infile,
                  strerror(errno));
    }

    if((ifd = open(*infile, O_RDONLY)) < 0)
      ERR_FAT(argv[0], "open: file for reading", *infile, strerror(errno));

    while(rlen = read(ifd, buf, BUF_SIZE)) {
      if(rlen < 0)
        ERR_FAT(argv[0], "reading of input file", *infile, strerror(errno));

      wtot = wlen = 0;
      while((wtot += wlen) < rlen) {
        if((wlen = write(ofd, buf+wtot, rlen-wtot)) < 0)
          ERR_FAT(argv[0], "writing of input file to pipe to grep", *infile,
                  strerror(errno));
        bp += wlen;
      }
    }

    if(close(ofd) < 0)
      ERR_FAT(argv[0], "closing output pipe", *infile, strerror(errno));
    if(close(ifd) < 0)
      ERR_FAT(argv[0], "closing input file", *infile, strerror(errno));
    ifd = ofd = -1;

    // no need to handle wait status
    waitpid(gpid, &wstatus, 0);
    waitpid(mpid, &wstatus, 0);
  }

  exit(EXIT_SUCCESS);
}
