/**
  * The Jon SHell
  * ECE357 Program 3
  **/
#include <ctype.h>
#include <errno.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <sys/wait.h>
#include <unistd.h>

#define SHELL "jsh"
#define PROMPT SHELL "$ "

#define ERR_FAT(prog, op, ctx, msg) {\
    dprintf(2, "%s: ERROR: %s \"%s\": %s\n", prog, op, ctx, msg);\
    exit(EXIT_FAILURE);\
  }
#define WARN(prog, op, ctx, msg)\
  dprintf(2, "%s: %s \"%s\": %s\n", prog, op, ctx, msg)

// structure parsed line
struct rd_out {
  // flag: 1 for append, 0 for trunc
  int append;
  char *file;
};
struct cmd_parse {
  char *cmd, **argv, *rd_in;
  struct rd_out rd_out, rd_err;
  int argc;
};

// global last status
int laststatus = 0;

// manage i/o redirection in file
void io_rd(char *file, int flags, int fd, char *rd_stream) {
  int rd_fd;
  char warn[40];
  if((rd_fd = open(file, flags, 0666)) < 0)
    sprintf(warn, "i/o redirection of (open) to %s", rd_stream);
  else if(dup2(rd_fd, fd) < 0)
    sprintf(warn, "i/o redirection of (dup2) to %s", rd_stream);
  else if(close(rd_fd) < 0)
    sprintf(warn, "unclean fd environment from i/o redirection (close) to %s",
            rd_stream);
  else
    return;
  WARN(SHELL, warn, file, strerror(errno));
  exit(1);
}

// handling line parsing; cmd_src is the source of cmds (stdin or interpreter)
void parse_line(char *line, FILE *cmd_src) {
  char *token, path_buf[4097];
  int argc_cap = 4, rd_fd;
  struct cmd_parse cmd_parse = {
    .cmd = NULL,
    .argv = (char **) malloc(argc_cap * sizeof(char *)),
    .rd_in = NULL,
    .rd_out = { 0, NULL },
    .rd_err = { 0, NULL },
    .argc = 1
  };
  struct rusage rusage;
  pid_t cpid;
  struct timeval cp_start, cp_end;
  int wstatus;

  // very basic comments: ignore lines starting with "#"
  if(*line == '#')
    return;

  // very basic tokenizing by whitespace
  token = strtok(line, " \t\n");
  if(!token)
    return;

  cmd_parse.cmd = token;
  cmd_parse.argv[0] = token;
  while(token = strtok(NULL, " \t\n")) {
    if(*token == '>') {
      cmd_parse.rd_out.append = *(token+1) == '>';
      cmd_parse.rd_out.file = token+1+cmd_parse.rd_out.append;
    } else if(*token == '2' && *(token+1) == '>') {
      cmd_parse.rd_err.append = *(token+2) == '>';
      cmd_parse.rd_err.file = token+2+cmd_parse.rd_err.append;
    } else if(*token == '<') {
      cmd_parse.rd_in = token+1;
    } else {
      if(cmd_parse.argc == argc_cap)
        if(!(cmd_parse.argv = (char **)
             realloc(cmd_parse.argv, (argc_cap*=2) * sizeof(char *))))
          WARN(SHELL, "allocating memory for argument parsing (realloc)",
               token, strerror(errno));
      cmd_parse.argv[cmd_parse.argc++] = token;
    }
  }
  // terminate argv with np
  if(cmd_parse.argc == argc_cap)
    if(!(cmd_parse.argv = (char **)
         realloc(cmd_parse.argv, (argc_cap+1) * sizeof(char *))))
      WARN(SHELL, "allocating memory for argument parsing (realloc)",
           "end token (NULL)", strerror(errno));
  cmd_parse.argv[cmd_parse.argc] = NULL;

  // shell built-ins
  if(!strcmp(cmd_parse.cmd, "pwd")) {
    if(!getcwd(path_buf, 4097)) {
      WARN("pwd", "getcwd", "", strerror(errno));
      laststatus = errno;
    } else
      dprintf(1, "%s\n", path_buf);
  } else if(!strcmp(cmd_parse.cmd, "cd")) {
    if(chdir(cmd_parse.argc == 1 ? getenv("HOME") : cmd_parse.argv[1]) < 0) {
      WARN("cd", "chdir", cmd_parse.argc == 1 ? "" : cmd_parse.argv[1],
           strerror(errno));
      laststatus = errno;
    }
  } else if(!strcmp(cmd_parse.cmd, "exit")) {
    // if invalid error code, return 2
    // bash does this; see https://askubuntu.com/a/892605/433872
    if(cmd_parse.argc > 1)
      for(char *c = cmd_parse.argv[1]; *c; c++)
        if(!(isdigit(*c) || (*c == '-' && c == cmd_parse.argv[1]))) {
          WARN(SHELL, "exit", cmd_parse.argv[1], "Numeric argument required");
          exit(2);
        }
    exit(cmd_parse.argc > 1 ? atoi(cmd_parse.argv[1]) : laststatus);
  }

  // fork, exec other programs
  else {
    gettimeofday(&cp_start, NULL);
    switch(cpid = fork()) {
      case -1:
        WARN(SHELL, cmd_parse.cmd, "fork", strerror(errno));
        break;

      // child
      case 0:
        // if not interactive mode, close interpreted script fd
        if(cmd_src != stdin) {
          if(fclose(cmd_src)) {
            WARN(SHELL, "closing script fd to initiate clean child fd env",
                 "close", strerror(errno));
          }
        }

        // i/o redirection
        if(cmd_parse.rd_in)
          io_rd(cmd_parse.rd_in, O_RDONLY, 0, "standard input");
        if(cmd_parse.rd_out.file)
          io_rd(cmd_parse.rd_out.file,
                O_WRONLY|O_CREAT|(cmd_parse.rd_out.append?O_APPEND:O_TRUNC),
                1, "standard output");
        if(cmd_parse.rd_err.file)
          io_rd(cmd_parse.rd_err.file,
                O_WRONLY|O_CREAT|(cmd_parse.rd_err.append?O_APPEND:O_TRUNC),
                2, "standard error");

        // exec; if unsuccessful, following lines to report error
        execvp(cmd_parse.cmd, cmd_parse.argv);
        WARN(SHELL, "exec", cmd_parse.cmd, strerror(errno));
        exit(127);

      // parent
      default:
        wait4(cpid, &wstatus, 0, &rusage);
        // return status from exit (laststatus) is the return value if normally
        // exited, and the whole status value if terminated with signal
        // (same behavior as bash)
        laststatus = WIFSIGNALED(wstatus) ? wstatus : WEXITSTATUS(wstatus);
        gettimeofday(&cp_end, NULL);
        dprintf(2, "%s: Child process %d exited ", SHELL, cpid);
        if(wstatus && !WIFSIGNALED(wstatus))
          dprintf(2, "with return value %d\n", WEXITSTATUS(wstatus));
        else if(wstatus)
          dprintf(2, "with signal %d (%s)\n",
                  WTERMSIG(wstatus), strsignal(WTERMSIG(wstatus)));
        else
          dprintf(2, "normally\n");
        dprintf(2, "%s: Real: %fs User: %fs Sys: %fs\n",
                SHELL,
                cp_end.tv_sec-cp_start.tv_sec+(cp_end.tv_usec-cp_start.tv_usec)
                  /1e6,
                rusage.ru_utime.tv_sec+rusage.ru_utime.tv_usec/1e6,
                rusage.ru_stime.tv_sec+rusage.ru_stime.tv_usec/1e6);
        free(cmd_parse.argv);
    }
  }
}

// driver function: handle interpreted scripts and start parse loop
int main(int argc, char **argv) {
  char *line_buf = NULL;
  size_t line_len = 0;
  FILE *cmd_in = stdin;
  
  // open command inputs as fd 3; will be closed to children after forking
  // expects first argument to be a fname since no other args are defined;
  // this is the same behavior as bash for non-option arguments
  if(argc > 1) {
    if(!(cmd_in = fopen(argv[1], "r")))
      ERR_FAT(SHELL, "Opening interpreter file", argv[1], strerror(errno));
  }

  // read, parse, execute command 
  // prints out prompt if not reading from script file
  errno = 0;
  while(cmd_in == stdin && dprintf(1, PROMPT),
        getline(&line_buf, &line_len, cmd_in) != -1)
    parse_line(line_buf, cmd_in);
  if(errno)
    ERR_FAT(SHELL, "Reading line (getline)", line_buf, strerror(errno));

  // cleanup and exit
  // this will only be called if EOF from interpreter
  free(line_buf);
  dprintf(1, "\nEOF read, exiting shell with exit code %d\n", laststatus);
  exit(EXIT_SUCCESS);
}
