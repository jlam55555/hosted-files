/**
  * rls (recursive ls), Jonathan Lam, 10/2/19
  */

#include <ctype.h>
#include <dirent.h>
#include <errno.h>
#include <grp.h>
#include <pwd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/sysmacros.h>
#include <sys/types.h>
#include <time.h>
#include <unistd.h>

// logging to stderr
#define ERROR_NON_FATAL(action, ctx, msg) \
  fprintf(stderr, "rls: %s \"%s\": %s\n", action, ctx, msg);

#define ERROR_FATAL(action, ctx, msg) {\
    fprintf(stderr, "rls: ERROR: %s \"%s\": %s\n", action, ctx, msg);\
    exit(EXIT_FAILURE);\
  }

// conditions for printing; used twice hence the macro
#define should_print()\
      (!optm || ((optm > 0 && stime - f_stat.st_mtime > optm)\
             || (optm < 0 && stime - f_stat.st_mtime <= -optm)))\
       && rperm(&f_stat, u_info)

// struct to store user/group info for optu
struct u_info {
  int optu;
  uid_t uid;
  gid_t *groups;
  int ngroups;
};

// get whether or not a user has read perms
int rperm(struct stat *f_stat, struct u_info *u_info) {
  // if optu not set or root, ignore
  if(!u_info->optu || !u_info->uid)
    return 1;

  // replicate unix perm checking; u->g->o
  if(f_stat->st_uid == u_info->uid)
    return f_stat->st_mode & S_IRUSR;
  for(int i = 0; i < u_info->ngroups; i++)
    if(u_info->groups[i] == f_stat->st_gid)
      return f_stat->st_mode & S_IRGRP;
  if(f_stat->st_mode & S_IROTH)
    return 1;
  return 0;
}

// print directory entry; handles decoding of stat info
void print_dirent(struct stat *f_stat, char *f_path) {
  // maximum path length in ext4: https://unix.stackexchange.com/a/32834/307410
  // maximum username length: https://serverfault.com/a/294122/332775
  char f_uname[33], f_gname[32], f_size[32], rl_buf[4097], f_link[4101],
       f_mtime[16], f_mode[11];
  struct passwd *f_user;
  struct group *f_group;
  int rl_len, f_blocks;

  // decode mode
  // get file type
  switch(f_stat->st_mode & S_IFMT) {
    case S_IFBLK: *f_mode = 'b'; break;
    case S_IFCHR: *f_mode = 'c'; break;
    case S_IFDIR: *f_mode = 'd'; break;
    case S_IFIFO: *f_mode = 'p'; break;
    case S_IFLNK: *f_mode = 'l'; break;
    case S_IFSOCK:*f_mode = 's'; break;
    case S_IFREG: *f_mode = '-'; break;
    default:      *f_mode = '?';
  }

  // permission bits
  f_mode[1] = f_stat->st_mode & S_IRUSR ? 'r' : '-';
  f_mode[2] = f_stat->st_mode & S_IWUSR ? 'w' : '-';
  f_mode[3] = f_stat->st_mode & S_IXUSR
            ? f_stat->st_mode & S_ISUID ? 's' : 'x'
            : f_stat->st_mode & S_ISUID ? 'S' : '-';
  f_mode[4] = f_stat->st_mode & S_IRGRP ? 'r' : '-';
  f_mode[5] = f_stat->st_mode & S_IWGRP ? 'w' : '-';
  f_mode[6] = f_stat->st_mode & S_IXGRP
            ? f_stat->st_mode & S_ISGID ? 's' : 'x'
            : f_stat->st_mode & S_ISGID ? 'S' : '-';
  f_mode[7] = f_stat->st_mode & S_IROTH ? 'r' : '-';
  f_mode[8] = f_stat->st_mode & S_IWOTH ? 'w' : '-';
  f_mode[9] = f_stat->st_mode & S_IXOTH
            ? f_stat->st_mode & S_ISVTX ? 't' : 'x'
            : f_stat->st_mode & S_ISVTX ? 'T' : '-';
  f_mode[10] = '\0';

  // get user info
  if(f_user = getpwuid(f_stat->st_uid))
    strcpy(f_uname, f_user->pw_name);
  else
    sprintf(f_uname, "%d", f_stat->st_uid);

  // get group info
  if(f_group = getgrgid(f_stat->st_gid))
    strcpy(f_gname, f_group->gr_name);
  else
    sprintf(f_gname, "%d", f_stat->st_gid);

  // if not char/block dev, get size in bytes
  // else get dev major, minor number
  if(!S_ISBLK(f_stat->st_mode) && !S_ISCHR(f_stat->st_mode))
    sprintf(f_size, "%ld", f_stat->st_size);
  else
    sprintf(f_size, "%d,%d", major(f_stat->st_rdev), minor(f_stat->st_rdev));

  // format date; if within the last year, do date/time (mon day time)
  // if longer do date/year (mon day year)
  // (1 year = 365*24*60*60 seconds = 31536000s)
  if(time(NULL) - f_stat->st_mtime < 31536000)
    strftime(f_mtime, 16, "%b %e %H:%M", localtime(&f_stat->st_mtime));
  else
    strftime(f_mtime, 16, "%b %e  %Y", localtime(&f_stat->st_mtime));

  // if symlink, get what it points to; otherwise, clear string
  if(S_ISLNK(f_stat->st_mode)) {
    if((rl_len = readlink(f_path, rl_buf, sizeof rl_buf - 1)) > 0)
      rl_buf[rl_len] = '\0';
    else
      ERROR_NON_FATAL("readlink", f_path, strerror(errno));

    sprintf(f_link, " -> %s", rl_buf);
  } else
    *f_link = '\0';

  // get number of blocks; contribution by Dave Kwong in the case
  // that fs has weird blocksize (adds 1 if not integer multiple of 1024)
  f_blocks = f_stat->st_blocks/2 + !!(f_stat->st_blocks % 1024);

  fprintf(stdout, " %8ld %6d %s %3d %-8s %-8s %8s %s %s%s\n",
          f_stat->st_ino, f_blocks, f_mode, f_stat->st_nlink,
          f_uname, f_gname, f_size, f_mtime, f_path, f_link);
  return;
};

// recursively walk fs and print; handles tree walking and deciding
// which inode entries should be printed
void rls(char *basedir, int optm, int optv,
         dev_t start_vol, struct u_info *u_info, time_t stime) {
  DIR *dir;
  struct dirent *dirent;
  // max path length is 4096: https://unix.stackexchange.com/a/32834/307410
  char f_path[4097];
  struct stat f_stat;

  if(!(dir = opendir(basedir)))
    // if no access to directory, exit here
    if(errno == EACCES) {
      ERROR_NON_FATAL("opening directory", basedir, strerror(errno));
      return;
    } else
      ERROR_FATAL("opening directory", basedir, strerror(errno));

  // this happens on the first call to rls (by main() driver)
  if(start_vol == -1) {
    if(stat(basedir, &f_stat) < 0)
      ERROR_FATAL("stat directory", basedir, strerror(errno));
    start_vol = f_stat.st_dev;

    // print if applicable
    if(should_print())
      print_dirent(&f_stat, basedir);
  }

  errno = 0;
  while(dirent = readdir(dir)) {
    // ignore ., ..
    if(!strcmp(dirent->d_name, ".") || !strcmp(dirent->d_name, ".."))
      continue;

    // stat inode
    sprintf(f_path, "%s%s%s", basedir,
            basedir[strlen(basedir)-1] == '/' ? "" : "/", dirent->d_name);
    if(lstat(f_path, &f_stat) < 0) {
      ERROR_NON_FATAL("stat", basedir, strerror(errno));
      if(errno == EACCES)
        return;
    }

    // print if applicable
    if(should_print())
      print_dirent(&f_stat, f_path);
    errno = 0;

    // if directory, recursively print filenames
    if(S_ISDIR(f_stat.st_mode))
      if(optv && f_stat.st_dev != start_vol) {
        ERROR_NON_FATAL("mount point", f_path, "Not crossing mount point");
      } else
        rls(f_path, optm, optv, start_vol, u_info, stime);
    errno = 0;
  }
  // error reading directory in above loop
  if(errno)
    ERROR_NON_FATAL("reading directory", basedir, strerror(errno));

  // close dir
  if(closedir(dir) < 0)
    ERROR_FATAL("closing directory", basedir, strerror(errno));

  return;
}

// driver for rls function
int main(int argc, char **argv) {
  int optv = 0, optm = 0, opt;
  long optu = -1;
  char *startdir = "./";
  struct passwd *optu_passwd;
  struct u_info u_info = { .optu = 0 };
  time_t stime;

  // parse args
  while((opt = getopt(argc, argv, "m:u:v")) != -1) {
    switch(opt) {
      case 'm':
        // silently fail if invalid number; atoi will return 0 if invalid
        optm = atoi(optarg);
        break;
      case 'u':
        errno = 0;
        if((isdigit(*optarg) && (optu_passwd = getpwuid(atoi(optarg))))
          || (!isdigit(*optarg) && (optu_passwd = getpwnam(optarg)))) {
          // get grouplist; once to get ngroups, second time to retrieve gl
          u_info.ngroups = 0;
          getgrouplist(optu_passwd->pw_name, optu_passwd->pw_gid,
                       NULL, &u_info.ngroups);
          u_info.groups = (gid_t *) malloc(u_info.ngroups * sizeof(gid_t *));
          getgrouplist(optu_passwd->pw_name, optu_passwd->pw_gid,
                       u_info.groups, &u_info.ngroups);
          u_info.optu = 1;
          u_info.uid = optu_passwd->pw_uid;
        }
        if(errno)
          ERROR_FATAL("(getpwuid/getpwnam) processing parameter -u", optarg,
                      strerror(errno));

        // user not found; non-fatal error
        if(!optu_passwd)
          ERROR_NON_FATAL("processing parameter -u", optarg, "User not found");

        break;
      case 'v':
        optv = 1;
        break;
    }
  }

  // iterate thu args if given; else rls on cwd
  stime = time(NULL);
  if(optind == argc)
    rls(".", optm, optv, -1, &u_info, stime);
  else
    while(optind++ != argc)
      rls(argv[optind-1], optm, optv, -1, &u_info, stime);

  // free dynamically allocated memory and exit
  if(u_info.optu)
    free(u_info.groups);
  exit(EXIT_SUCCESS);
}

/**
  * Notes about this program:
  * - Formatting (padding) is not dynamic, but not the point of this project.
  * - Repeated -m and -u opts overwrite one another -- again, not the point
  *   of this project and could be implemented with more time.
  * - Unlike find, this doesn't escape some chars (e.g., backslash and space),
  *   so those show up on the diff.
  * - Some of the output messages were changed a little to be more consistent
  *   and similar to the output of find.
  */
