#include <errno.h>
#include <fcntl.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include <unistd.h>

// test program vars; chosen arbitrarily and tweakable
#define TMP_PATH "/tmp/mtest.tmp"
#define TST_IND 3
#define TST_LEN 324
#define TST_CHR 'A'

// less strict error reporting, since these errors shouldn't happen
int err_chk(int res, char *cmd_name) {
  if(res<0) {
    dprintf(2, "./mtest: ERROR: failed on %s: %s.\n",
            cmd_name, strerror(errno));
    exit(EXIT_FAILURE);
  }
  dprintf(2, "./mtest: Performing %s.\n", cmd_name);
  return res;
}

// auxiliary fns
void sig_trap(int sig_num) {
  dprintf(2, "./mtest: Received signal %d: %s. Exiting.\n",
          sig_num, strsignal(sig_num));
  exit(sig_num);
}
void trap_all_sigs() {
  for(int i=1; i<=31; i++)
    signal(i, sig_trap);
}
int create_tmp_file() {
  int fd;
  char buf[] = "Hello, world!";

  // create and fill with some (known) bytes
  fd = err_chk(open(TMP_PATH, O_CREAT|O_RDWR|O_TRUNC), "creat tmpfile");
  err_chk(write(fd, buf, sizeof buf), "fill some data to tmpfile");
  err_chk(unlink(TMP_PATH), "unlink tmpfile");

  return fd;
}

// test 1: PROT_READ violation
void test_1() {
  char *p, prev, post;

  trap_all_sigs();
  p = (char *) mmap(NULL, 4096, PROT_READ, MAP_ANON|MAP_SHARED, -1, 0);
  err_chk(-(p==MAP_FAILED), "mmap tmpfile with MAP_ANON|MAP_SHARED");

  prev = p[TST_IND];
  dprintf(2, "./mtest: p[TST_IND]==%x.\n", prev);
  dprintf(2, "./mtest: attempting write p[TST_IND]=%x.\n", prev+1);
  post = ++p[TST_IND];

  // diagnostic message and cleanup
  err_chk(munmap(p, 4096), "munmap tmpfile");
  dprintf(2, "./mtest: test byte: pre-write: %x post-write %x.\n", prev, post);
  exit(-(prev==post));
}

// tests 2/3: writing to MAP_SHARED/MAP_PRIVATE
void test_23(int is_shared) {
  int fd;
  char *p, prev, post;

  fd = create_tmp_file();
  p = (char *) mmap(NULL, 4096, PROT_READ|PROT_WRITE, 
                    MAP_FILE|(is_shared?MAP_SHARED:MAP_PRIVATE), fd, 0);
  err_chk(-(p==MAP_FAILED), "mmap tmpfile with MAP_SHARED or MAP_PRIVATE");
  
  prev = p[TST_IND];
  post = ++p[TST_IND];

  // see if regular read call works
  err_chk(lseek(fd, TST_IND, SEEK_SET), "lseek in tmpfile");
  err_chk(read(fd, &post, 1), "read in tmpfile");

  // diagnostic messae and cleanup
  err_chk(munmap(p, 4096), "munmap tmpfile");
  err_chk(close(fd), "close tmpfile");
  dprintf(2, "./mtest: test byte: pre-write: %x post-write %x.\n", prev, post);
  exit(prev==post);
}

// test 4: writing into a hole
void test_4() {
  int fd;
  char *p, post;

  fd = create_tmp_file();
  p = (char *) mmap(NULL, TST_LEN, PROT_READ|PROT_WRITE, MAP_FILE|MAP_SHARED,
                    fd, 0);
  err_chk(-(p==MAP_FAILED), "mmap tmpfile with MAP_SHARED");

  p[TST_LEN] = TST_CHR;
  err_chk(lseek(fd, TST_LEN+16, SEEK_SET), "lseek to TST_LEN+16 in tmpfile");
  err_chk(write(fd, " ", 1), "write to TST_LEN+16 in tmpfile");
  err_chk(lseek(fd, TST_LEN, SEEK_SET), "lseek to TST_LEN in tmpfile");
  err_chk(read(fd, &post, 1), "read in tmpfile");

  // diagnostic messae and cleanup
  err_chk(munmap(p, TST_LEN), "munmap tmpfile");
  err_chk(close(fd), "close tmpfile");
  dprintf(2, "./mtest: test byte: %x; byte at pos TST_LEN+1: %x.\n", TST_CHR, post);
  exit(1-(TST_CHR==post));
}

// usage: ./a.out [TEST_NUM]
int main(int argc, char **argv) {
  if(argc > 1)
    switch(**(argv+1)) {
    case '1': test_1(); break;
    case '2': test_23(1); break;
    case '3': test_23(0); break;
    case '4': test_4(); break;
    }
  exit(EXIT_FAILURE);
}
