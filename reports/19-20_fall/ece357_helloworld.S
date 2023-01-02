.data
  str:  .string "Hello, world\!\n"

.text
  # write(1, "Hello, world!\n", 14)
  movl  $0x4,%eax   # write syscall# = 4
  movl  $0x1,%ebx   # fd = 1
  movl  $str,%ecx   # addr of string "Hello, world!\n"
  movl  $14,%edx    # len of string "Hello, world!\n"
  int   $0x80       # invoke syscall

  # exit(0)
  movl  %eax,%ebx   # exit code = retval of write
  movl  $0x1,%eax   # exit syscall# = 1
  int   $0x80       # invoke syscall
