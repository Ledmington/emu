#
# Input file for x86 instruction decoding
#
# Lines that are:
# - empty
# - blank
# - beginning with '#'
# will be treated as comments and, therefore, ignored.
#
# All other lines must follow the following format:
# - the expected output: the instruction written in human-readable form
# - a single '|' character to separate the input and the expected output
# - the binary (hexadecimal) representation of the instruction
#
# To have a reference which is a bit more usable than the Intel
# Software Developer Manual, you can use this:
# https://defuse.ca/online-x86-assembler.htm
#

# No-op
nop | 90

# Call
call 0xffffffffffffff1d | e8 18 ff ff ff

# Je
je 0x2e0e1 | 0f 84 db e0 02 00

# Jmp
jmp 0x2e301 | e9 fc e2 02 00

# lea
lea eax,[r10-0xf]                | 41 8d 42 f1
lea eax,[r11+0x1]                | 41 8d 43 01
lea eax,[r11-0x19]               | 41 8d 43 e7
lea eax,[r13*8-0x1]              | 42 8d 04 ed ff ff ff
lea eax,[r13+0x1]                | 41 8d 45 01
lea eax,[r15+0x1]                | 41 8d 47 01
lea eax,[r15+0x2]                | 41 8d 47 02
lea eax,[r8-0xf]                 | 41 8d 40 f1
lea eax,[r9+r9*1-0x1]            | 43 8d 44 09 ff
lea eax,[rax+rax*1]              | 8d 04 00
lea eax,[rax+rax*4+0x1]          | 8d 44 80 01
lea eax,[rax+rdx*1-0x71]         | 8d 44 10 8f
lea eax,[rbx+0x1]                | 8d 43 01
lea eax,[rbx+rbx*1+0x2]          | 8d 44 1b 02
lea eax,[rbx-0x1]                | 8d 43 ff
lea eax,[rbx-0x2]                | 8d 43 fe
lea eax,[rbx-0x30]               | 8d 43 d0
lea eax,[rbx-0x3]                | 8d 43 fd
lea eax,[rcx+0x14]               | 8d 41 14
lea eax,[rcx+0x1]                | 8d 41 01
lea eax,[rcx-0x63]               | 8d 41 9d
lea eax,[rcx-0x70]               | 8d 41 90
lea eax,[rdi*8-0x1]              | 8d 04 fd ff ff ff ff
lea eax,[rdi+0x1]                | 8d 47 01
lea eax,[rdi+rdi*1-0x1]          | 8d 44 3f ff
lea eax,[rdi-0x1]                | 8d 47 ff
lea eax,[rdi-0x2]                | 8d 47 fe
lea eax,[rdi-0x824]              | 8d 87 dc f7 ff ff
lea eax,[rdx+0x1]                | 8d 42 01
lea eax,[rdx-0x1]                | 8d 42 ff
lea eax,[rdx-0x1c]               | 8d 42 e4
lea eax,[rdx-0x30]               | 8d 42 d0
lea eax,[rdx-0x3]                | 8d 42 fd
lea eax,[rdx-0x77]               | 8d 42 89
lea eax,[rsi+0x1]                | 8d 46 01
lea eax,[rsi+0x2]                | 8d 46 02
lea eax,[rsi-0x1]                | 8d 46 ff
lea eax,[rsi-0x29]               | 8d 46 d7
lea eax,[rsi-0x2]                | 8d 46 fe
lea eax,[rsi-0x8]                | 8d 46 f8
lea ebp,[r10-0x30]               | 41 8d 6a d0
lea ebp,[r12+0x1]                | 41 8d 6c 24 01
lea ebp,[r15+0x1]                | 41 8d 6f 01
lea ebp,[rax-0x30]               | 8d 68 d0
lea ebp,[rbp+rax*1+0x1]          | 8d 6c 05 01
lea ebp,[rbx+0x1]                | 8d 6b 01
lea ebp,[rcx+0x1]                | 8d 69 01
lea ebp,[rcx-0xd800]             | 8d a9 00 28 ff ff
lea ebp,[rsi-0x30]               | 8d 6e d0
lea ebx,[r10+0x1]                | 41 8d 5a 01
lea ebx,[r12+0x1]                | 41 8d 5c 24 01
lea ebx,[r12+rax*1]              | 41 8d 1c 04
lea ebx,[r15+0x2]                | 41 8d 5f 02
lea ebx,[r15-0x1]                | 41 8d 5f ff
lea ebx,[r8-0x1]                 | 41 8d 58 ff
lea ebx,[rax+0x1]                | 8d 58 01
lea ebx,[rax+rbx*2]              | 8d 1c 58
lea ebx,[rax-0x61]               | 8d 58 9f
lea ebx,[rdi+rax*1]              | 8d 1c 07
lea ebx,[rdx+0x1]                | 8d 5a 01
lea ebx,[rdx-0x61]               | 8d 5a 9f
lea ebx,[rsi+0x1]                | 8d 5e 01
lea ebx,[rsi+rbx*2+0x1]          | 8d 5c 5e 01
lea ebx,[rsi-0x1]                | 8d 5e ff
lea ecx,[r10-0x6]                | 41 8d 4a fa
lea ecx,[r11-0x22]               | 41 8d 4b de
lea ecx,[r11-0x30]               | 41 8d 4b d0
lea ecx,[r13+r12*1+0x0]          | 43 8d 4c 25 00
lea ecx,[r14+0x2]                | 41 8d 4e 02
lea ecx,[r14+0x3]                | 41 8d 4e 03
lea ecx,[r14-0x1]                | 41 8d 4e ff
lea ecx,[r15+0x7]                | 41 8d 4f 07
lea ecx,[r8+0x1]                 | 41 8d 48 01
lea ecx,[r8+r11*1]               | 43 8d 0c 18
lea ecx,[r8-0x1]                 | 41 8d 48 ff
lea ecx,[r8-0x1c]                | 41 8d 48 e4
lea ecx,[r8-0x24]                | 41 8d 48 dc
lea ecx,[r8-0x61]                | 41 8d 48 9f
lea ecx,[r9+0x2]                 | 41 8d 49 02
lea ecx,[r9+0x3]                 | 41 8d 49 03
lea ecx,[r9+r9*1+0x1]            | 43 8d 4c 09 01
lea ecx,[r9+rax*1]               | 41 8d 0c 01
lea ecx,[r9-0x1c]                | 41 8d 49 e4
lea ecx,[r9-0x2a]                | 41 8d 49 d6
lea ecx,[r9-0x44]                | 41 8d 49 bc
lea ecx,[rax+0x1]                | 8d 48 01
lea ecx,[rax+0x80]               | 8d 88 80 00 00 00
lea ecx,[rax+rax*1]              | 8d 0c 00
lea ecx,[rax+rcx*4-0x30]         | 8d 4c 88 d0
lea ecx,[rax+rcx*4-0x37]         | 8d 4c 88 c9
lea ecx,[rax-0x1]                | 8d 48 ff
lea ecx,[rax-0x1c]               | 8d 48 e4
lea ecx,[rax-0x24]               | 8d 48 dc
lea ecx,[rax-0x2]                | 8d 48 fe
lea ecx,[rax-0x30]               | 8d 48 d0
lea ecx,[rax-0x31]               | 8d 48 cf
lea ecx,[rax-0x41]               | 8d 48 bf
lea ecx,[rax-0x43]               | 8d 48 bd
lea ecx,[rax-0x4b]               | 8d 48 b5
lea ecx,[rax-0x5a]               | 8d 48 a6
lea ecx,[rax-0x61]               | 8d 48 9f
lea ecx,[rax-0x72]               | 8d 48 8e
lea ecx,[rbp+0x1]                | 8d 4d 01
lea ecx,[rbp+0x1c]               | 8d 4d 1c
lea ecx,[rbp+0x3]                | 8d 4d 03
lea ecx,[rbp+rbp*1+0x1]          | 8d 4c 2d 01
lea ecx,[rbp+rbx*1+0x0]          | 8d 4c 1d 00
lea ecx,[rbp-0x45]               | 8d 4d bb
lea ecx,[rbp-0x69]               | 8d 4d 97
lea ecx,[rbx-0x1]                | 8d 4b ff
lea ecx,[rbx-0x43]               | 8d 4b bd
lea ecx,[rdi-0x30]               | 8d 4f d0
lea ecx,[rdi-0x9]                | 8d 4f f7
lea ecx,[rdx+0x1]                | 8d 4a 01
lea ecx,[rdx+0x8]                | 8d 4a 08
lea ecx,[rdx+rdx*1]              | 8d 0c 12
lea ecx,[rdx+rdx*8]              | 8d 0c d2
lea ecx,[rdx+rsi*1]              | 8d 0c 32
lea ecx,[rdx-0x1]                | 8d 4a ff
lea ecx,[rdx-0x30]               | 8d 4a d0
lea ecx,[rdx-0x4b]               | 8d 4a b5
lea ecx,[rdx-0x57]               | 8d 4a a9
lea ecx,[rdx-0x61]               | 8d 4a 9f
lea ecx,[rdx-0x63]               | 8d 4a 9d
lea ecx,[rsi+0x1]                | 8d 4e 01
lea ecx,[rsi+0x8]                | 8d 4e 08
lea ecx,[rsi+r8*1]               | 42 8d 0c 06
lea ecx,[rsi-0x1]                | 8d 4e ff
lea ecx,[rsi-0x1c]               | 8d 4e e4
lea ecx,[rsi-0x2b]               | 8d 4e d5
lea ecx,[rsi-0x34]               | 8d 4e cc
lea edi,[r10-0x6]                | 41 8d 7a fa
lea edi,[r13+0x1]                | 41 8d 7d 01
lea edi,[r14+0x1]                | 41 8d 7e 01
lea edi,[r14+rax*1-0x1]          | 41 8d 7c 06 ff
lea edi,[r8+0x2]                 | 41 8d 78 02
lea edi,[r8-0x2]                 | 41 8d 78 fe
lea edi,[r9-0xc]                 | 41 8d 79 f4
lea edi,[rax+0x1]                | 8d 78 01
lea edi,[rax+0x2]                | 8d 78 02
lea edi,[rbp+0x1]                | 8d 7d 01
lea edi,[rbp+rbx*1+0x1]          | 8d 7c 1d 01
lea edi,[rbp-0x1]                | 8d 7d ff
lea edi,[rbx+0x1]                | 8d 7b 01
lea edi,[rbx+rbx*1]              | 8d 3c 1b
lea edi,[rbx-0x2]                | 8d 7b fe
lea edi,[rbx-0x30]               | 8d 7b d0
lea edi,[rcx-0x30]               | 8d 79 d0
lea edi,[rcx-0xa]                | 8d 79 f6
lea edi,[rsi+0x1]                | 8d 7e 01
lea edi,[rsi+0x8]                | 8d 7e 08
lea edi,[rsi-0x2b]               | 8d 7e d5
lea edx,[r10+0x1]                | 41 8d 52 01
lea edx,[r10+r10*1+0x1]          | 43 8d 54 12 01
lea edx,[r10+r11*1+0x7]          | 43 8d 54 1a 07
lea edx,[r10+rbp*1]              | 41 8d 14 2a
lea edx,[r11+0xc0]               | 41 8d 93 c0 00 00 00
lea edx,[r11+rcx*1-0x37]         | 41 8d 54 0b c9
lea edx,[r12-0x1]                | 41 8d 54 24 ff
lea edx,[r13-0x2]                | 41 8d 55 fe
lea edx,[r14+0x1]                | 41 8d 56 01
lea edx,[r14+0x3]                | 41 8d 56 03
lea edx,[r14+rbp*1]              | 41 8d 14 2e
lea edx,[r14-0x1]                | 41 8d 56 ff
lea edx,[r14-0x3]                | 41 8d 56 fd
lea edx,[r15+0x1]                | 41 8d 57 01
lea edx,[r8+0xe4]                | 41 8d 90 e4 00 00 00
lea edx,[r8+r8*4]                | 43 8d 14 80
lea edx,[r8-0x1]                 | 41 8d 50 ff
lea edx,[r8-0x9]                 | 41 8d 50 f7
lea edx,[r9+0xd8]                | 41 8d 91 d8 00 00 00
lea edx,[r9-0x1]                 | 41 8d 51 ff
lea edx,[rax+0x1]                | 8d 50 01
lea edx,[rax+0x3]                | 8d 50 03
lea edx,[rax+rdi*1-0x30]         | 8d 54 38 d0
lea edx,[rax-0x1]                | 8d 50 ff
lea edx,[rax-0x30]               | 8d 50 d0
lea edx,[rax-0x41]               | 8d 50 bf
lea edx,[rax-0x49]               | 8d 50 b7
lea edx,[rax-0x61]               | 8d 50 9f
lea edx,[rbp+0x1d]               | 8d 55 1d
lea edx,[rbp-0x2]                | 8d 55 fe
lea edx,[rbx+0x1]                | 8d 53 01
lea edx,[rbx+0x334]              | 8d 93 34 03 00 00
lea edx,[rbx+r12*1+0xf0]         | 42 8d 94 23 f0 00 00
lea edx,[rbx+r12*1]              | 42 8d 14 23
lea edx,[rbx-0x1]                | 8d 53 ff
lea edx,[rbx-0x2]                | 8d 53 fe
lea edx,[rbx-0x49]               | 8d 53 b7
lea edx,[rbx-0x61]               | 8d 53 9f
lea edx,[rcx+0x1]                | 8d 51 01
lea edx,[rcx+rcx*1]              | 8d 14 09
lea edx,[rcx+rdx*1+0x7]          | 8d 54 11 07
lea edx,[rcx-0x1a]               | 8d 51 e6
lea edx,[rcx-0x1f]               | 8d 51 e1
lea edx,[rcx-0x30]               | 8d 51 d0
lea edx,[rcx-0x49]               | 8d 51 b7
lea edx,[rdi+0x1]                | 8d 57 01
lea edx,[rdi+rcx*1-0x30]         | 8d 54 0f d0
lea edx,[rdi-0x327]              | 8d 97 d9 fc ff ff
lea edx,[rdi-0x3]                | 8d 57 fd
lea edx,[rdx+0x2]                | 8d 52 02
lea edx,[rdx+rdx*2]              | 8d 14 52
lea edx,[rsi+0x10]               | 8d 56 10
lea edx,[rsi+0xcc]               | 8d 96 cc 00 00 00
lea edx,[rsi-0x61]               | 8d 56 9f
lea esi,[r10-0x1]                | 41 8d 72 ff
lea esi,[r10-0x9]                | 41 8d 72 f7
lea esi,[r11+0x1]                | 41 8d 73 01
lea esi,[r11-0x30]               | 41 8d 73 d0
lea esi,[r12+0x1]                | 41 8d 74 24 01
lea esi,[r12-0x1]                | 41 8d 74 24 ff
lea esi,[r13+0x1]                | 41 8d 75 01
lea esi,[r13-0x1]                | 41 8d 75 ff
lea esi,[r14+0x1]                | 41 8d 76 01
lea esi,[r14+0x2]                | 41 8d 76 02
lea esi,[r14-0x1]                | 41 8d 76 ff
lea esi,[r14-0x2d]               | 41 8d 76 d3
lea esi,[r15+0x1]                | 41 8d 77 01
lea esi,[r15-0x1]                | 41 8d 77 ff
lea esi,[r8-0x3]                 | 41 8d 70 fd
lea esi,[r8-0xc]                 | 41 8d 70 f4
lea esi,[r9-0x1]                 | 41 8d 71 ff
lea esi,[r9-0x1f]                | 41 8d 71 e1
lea esi,[rax+0x1]                | 8d 70 01
lea esi,[rax-0x49]               | 8d 70 b7
lea esi,[rbp+0x1]                | 8d 75 01
lea esi,[rbx+0x1]                | 8d 73 01
lea esi,[rbx+0x3b4]              | 8d b3 b4 03 00 00
lea esi,[rbx-0x1]                | 8d 73 ff
lea esi,[rbx-0x31]               | 8d 73 cf
lea esi,[rcx+r8*1]               | 42 8d 34 01
lea esi,[rcx+rcx*1]              | 8d 34 09
lea esi,[rcx+rcx*2]              | 8d 34 49
lea esi,[rcx-0x19]               | 8d 71 e7
lea esi,[rcx-0x1]                | 8d 71 ff
lea esi,[rcx-0x1c]               | 8d 71 e4
lea esi,[rcx-0x30]               | 8d 71 d0
lea esi,[rdi+rdi*1]              | 8d 34 3f
lea esi,[rdi-0x6]                | 8d 77 fa
lea esi,[rdx+0x1]                | 8d 72 01
lea esi,[rdx+0x30]               | 8d 72 30
lea esi,[rdx-0x1]                | 8d 72 ff
lea esi,[rdx-0x43]               | 8d 72 bd
lea esi,[rdx-0x5ab]              | 8d b2 55 fa ff ff
lea esi,[rsi+0x1]                | 8d 76 01
lea r10,[r11+0x18]               | 4d 8d 53 18
lea r10,[r12+0x1]                | 4d 8d 54 24 01
lea r10,[r12+rax*1]              | 4d 8d 14 04
lea r10,[r13+rax*1+0x0]          | 4d 8d 54 05 00
lea r10,[r13+rdx*8+0x0]          | 4d 8d 54 d5 00
lea r10,[r14+0x1]                | 4d 8d 56 01
lea r10,[r15+r12*1]              | 4f 8d 14 27
lea r10,[r8+r8*1]                | 4f 8d 14 00
lea r10,[r8+r8*2]                | 4f 8d 14 40
lea r10,[r9*8+0x0]               | 4e 8d 14 cd 00 00 00
lea r10,[r9+0x1]                 | 4d 8d 51 01
lea r10,[r9+0x8]                 | 4d 8d 51 08
lea r10,[r9+r8*1]                | 4f 8d 14 01
lea r10,[r9+r9*1]                | 4f 8d 14 09
lea r10,[r9+r9*2]                | 4f 8d 14 49
lea r10,[rax+0x1]                | 4c 8d 50 01
lea r10,[rax+r8*1]               | 4e 8d 14 00
lea r10,[rax-0x2]                | 4c 8d 50 fe
lea r10,[rbp+0x12]               | 4c 8d 55 12
lea r10,[rbp+0x1]                | 4c 8d 55 01
lea r10,[rbp+r15*1+0x0]          | 4e 8d 54 3d 00
lea r10,[rbx+0x2]                | 4c 8d 53 02
lea r10,[rbx+0x4]                | 4c 8d 53 04
lea r10,[rbx+rax*1]              | 4c 8d 14 03
lea r10,[rcx*8+0x17]             | 4c 8d 14 cd 17 00 00
lea r10,[rip+0x9042c]            | 4c 8d 15 2c 04 09 00
lea r10,[rip+0x904f2]            | 4c 8d 15 f2 04 09 00
lea r10,[rip+0x91290]            | 4c 8d 15 90 12 09 00
lea r10,[rsi+0x8]                | 4c 8d 56 08
lea r10,[rsp+0x10]               | 4c 8d 54 24 10
lea r10,[rsp+0x148]              | 4c 8d 94 24 48 01 00
lea r10,[rsp+0x188]              | 4c 8d 94 24 88 01 00
lea r10,[rsp+0x20]               | 4c 8d 54 24 20
lea r10,[rsp+0x288]              | 4c 8d 94 24 88 02 00
lea r10,[rsp+0x38]               | 4c 8d 54 24 38
lea r10,[rsp+0x3e8]              | 4c 8d 94 24 e8 03 00
lea r10,[rsp+0x57]               | 4c 8d 54 24 57
lea r10,[rsp+0x60]               | 4c 8d 54 24 60
lea r10,[rsp+0x68]               | 4c 8d 54 24 68
lea r10,[rsp+0x70]               | 4c 8d 54 24 70
lea r10,[rsp+0xa0]               | 4c 8d 94 24 a0 00 00
lea r10,[rsp+0xf]                | 4c 8d 54 24 0f
lea r10d,[r10+rax*1]             | 45 8d 14 02
lea r10d,[r10+rdx*1]             | 45 8d 14 12
lea r10d,[r11+0x1]               | 45 8d 53 01
lea r10d,[r11+0x2]               | 45 8d 53 02
lea r10d,[r12+r12*1+0x2]         | 47 8d 54 24 02
lea r10d,[r12+r8*1-0x30]         | 47 8d 54 04 d0
lea r10d,[r12+rbx*1]             | 45 8d 14 1c
lea r10d,[r14+rdx*1+0x3]         | 45 8d 54 16 03
lea r10d,[r14-0x3]               | 45 8d 56 fd
lea r10d,[r14-0x4]               | 45 8d 56 fc
lea r10d,[r8+0x3]                | 45 8d 50 03
lea r10d,[r8-0x1]                | 45 8d 50 ff
lea r10d,[rax-0x1]               | 44 8d 50 ff
lea r10d,[rbp+0x30]              | 44 8d 55 30
lea r10d,[rbp+0x57]              | 44 8d 55 57
lea r10d,[rbx-0x1]               | 44 8d 53 ff
lea r10d,[rcx+0x1]               | 44 8d 51 01
lea r10d,[rcx+rax*1]             | 44 8d 14 01
lea r10d,[rcx-0x30]              | 44 8d 51 d0
lea r10d,[rdi+0x1]               | 44 8d 57 01
lea r10d,[rdx+0x1]               | 44 8d 52 01
lea r10d,[rdx-0x1]               | 44 8d 52 ff
lea r10d,[rdx-0x4]               | 44 8d 52 fc
lea r11,[r10+0x10]               | 4d 8d 5a 10
lea r11,[r10+0x8]                | 4d 8d 5a 08
lea r11,[r10+rsi*8-0x10]         | 4d 8d 5c f2 f0
lea r11,[r12+0x1]                | 4d 8d 5c 24 01
lea r11,[r12+0x58]               | 4d 8d 5c 24 58
lea r11,[r12+rbp*1]              | 4d 8d 1c 2c
lea r11,[r13+0x70]               | 4d 8d 5d 70
lea r11,[r13+0x8]                | 4d 8d 5d 08
lea r11,[r13+r13*1+0x0]          | 4f 8d 5c 2d 00
lea r11,[r14+0x48baa0]           | 4d 8d 9e a0 ba 48 00
lea r11,[r14+r12*1]              | 4f 8d 1c 26
lea r11,[r15+0x8]                | 4d 8d 5f 08
lea r11,[r15+r10*1]              | 4f 8d 1c 17
lea r11,[r8+r9*1-0x18]           | 4f 8d 5c 08 e8
lea r11,[r8+rax*1]               | 4d 8d 1c 00
lea r11,[r9+0x4]                 | 4d 8d 59 04
lea r11,[r9+r9*2]                | 4f 8d 1c 49
lea r11,[r9+rax*1]               | 4d 8d 1c 01
lea r11,[rax+0x1]                | 4c 8d 58 01
lea r11,[rax+0x6]                | 4c 8d 58 06
lea r11,[rax+0x8]                | 4c 8d 58 08
lea r11,[rbp+r10*1+0x10]         | 4e 8d 5c 15 10
lea r11,[rbx+rbx*1]              | 4c 8d 1c 1b
lea r11,[rcx+0x1]                | 4c 8d 59 01
lea r11,[rcx+0x8]                | 4c 8d 59 08
lea r11,[rdx+0x1]                | 4c 8d 5a 01
lea r11,[rdx+rsi*8+0x8]          | 4c 8d 5c f2 08
lea r11,[rsp+0x107]              | 4c 8d 9c 24 07 01 00
lea r11,[rsp+0x5c]               | 4c 8d 5c 24 5c
lea r11,[rsp+0x5d0]              | 4c 8d 9c 24 d0 05 00
lea r11,[rsp+0x6f0]              | 4c 8d 9c 24 f0 06 00
lea r11,[rsp+0xa0]               | 4c 8d 9c 24 a0 00 00
lea r11,[rsp+0xc0]               | 4c 8d 9c 24 c0 00 00
lea r11,[rsp+0xf]                | 4c 8d 5c 24 0f
lea r11d,[r12-0x24]              | 45 8d 5c 24 dc
lea r11d,[r12-0x31]              | 45 8d 5c 24 cf
lea r11d,[r13-0x5]               | 45 8d 5d fb
lea r11d,[r15*8+0x0]             | 46 8d 1c fd 00 00 00
lea r11d,[r15-0x825]             | 45 8d 9f db f7 ff ff
lea r11d,[r8+0x2]                | 45 8d 58 02
lea r11d,[r8+0x2af]              | 45 8d 98 af 02 00 00
lea r11d,[r8+rcx*1]              | 45 8d 1c 08
lea r11d,[r9+0x2]                | 45 8d 59 02
lea r11d,[rax-0x1]               | 44 8d 58 ff
lea r11d,[rax-0x61]              | 44 8d 58 9f
lea r11d,[rbx+0x14]              | 44 8d 5b 14
lea r11d,[rcx+r8*1]              | 46 8d 1c 01
lea r11d,[rcx-0x1]               | 44 8d 59 ff
lea r11d,[rdi+0x2]               | 44 8d 5f 02
lea r11d,[rdi-0xc]               | 44 8d 5f f4
lea r11d,[rdx+0x2]               | 44 8d 5a 02
lea r11d,[rdx+0x30]              | 44 8d 5a 30
lea r11d,[rdx+0x57]              | 44 8d 5a 57
lea r12,[r11+0x8]                | 4d 8d 63 08
lea r12,[r11+0xf]                | 4d 8d 63 0f
lea r12,[r12+rax*1+0x1]          | 4d 8d 64 04 01
lea r12,[r13+0x1]                | 4d 8d 65 01
lea r12,[r13+r11*1+0x0]          | 4f 8d 64 1d 00
lea r12,[r14+0x1]                | 4d 8d 66 01
lea r12,[r14+0x2]                | 4d 8d 66 02
lea r12,[r14+0x8]                | 4d 8d 66 08
lea r12,[r15+0x1]                | 4d 8d 67 01
lea r12,[r15+0xb]                | 4d 8d 67 0b
lea r12,[r15+r10*1+0x18]         | 4f 8d 64 17 18
lea r12,[r15+r11*8+0x18]         | 4f 8d 64 df 18
lea r12,[r8+0x1]                 | 4d 8d 60 01
lea r12,[r9+rcx*1]               | 4d 8d 24 09
lea r12,[rax*4+0x4]              | 4c 8d 24 85 04 00 00
lea r12,[rax+0x10]               | 4c 8d 60 10
lea r12,[rax+0x18]               | 4c 8d 60 18
lea r12,[rax+0x1]                | 4c 8d 60 01
lea r12,[rax+rbx*1]              | 4c 8d 24 18
lea r12,[rax+rbx*8]              | 4c 8d 24 d8
lea r12,[rbp*8+0x8]              | 4c 8d 24 ed 08 00 00
lea r12,[rbp+0x1]                | 4c 8d 65 01
lea r12,[rbp+rdx*1+0x0]          | 4c 8d 64 15 00
lea r12,[rbp-0x1b0]              | 4c 8d a5 50 fe ff ff
lea r12,[rbp-0x2b0]              | 4c 8d a5 50 fd ff ff
lea r12,[rbx+0x10]               | 4c 8d 63 10
lea r12,[rbx+0x1]                | 4c 8d 63 01
lea r12,[rbx+0x8]                | 4c 8d 63 08
lea r12,[rbx+0xb]                | 4c 8d 63 0b
lea r12,[rcx+0x1]                | 4c 8d 61 01
lea r12,[rcx+0x2]                | 4c 8d 61 02
lea r12,[rcx+r15*1]              | 4e 8d 24 39
lea r12,[rdi*8+0x17]             | 4c 8d 24 fd 17 00 00
lea r12,[rdi+0x10]               | 4c 8d 67 10
lea r12,[rdi+0x4]                | 4c 8d 67 04
lea r12,[rdi+rsi*1-0x1]          | 4c 8d 64 37 ff
lea r12,[rdx+0x10]               | 4c 8d 62 10
lea r12,[rdx+rcx*8]              | 4c 8d 24 ca
lea r12,[rdx+rdi*1]              | 4c 8d 24 3a
lea r12,[rdx-0x1]                | 4c 8d 62 ff
lea r12,[rip+0x7a702]            | 4c 8d 25 02 a7 07 00
lea r12,[rip+0x8c4e9]            | 4c 8d 25 e9 c4 08 00
lea r12,[rsi+rbx*1]              | 4c 8d 24 1e
lea r12,[rsp+0x107]              | 4c 8d a4 24 07 01 00
lea r12,[rsp+0x10]               | 4c 8d 64 24 10
lea r12,[rsp+0x1e8]              | 4c 8d a4 24 e8 01 00
lea r12,[rsp+0x208]              | 4c 8d a4 24 08 02 00
lea r12,[rsp+0x270]              | 4c 8d a4 24 70 02 00
lea r12,[rsp+0x3a0]              | 4c 8d a4 24 a0 03 00
lea r12,[rsp+0x4b0]              | 4c 8d a4 24 b0 04 00
lea r12,[rsp+0x58]               | 4c 8d 64 24 58
lea r12,[rsp+0x80]               | 4c 8d a4 24 80 00 00
lea r12,[rsp+0x810]              | 4c 8d a4 24 10 08 00
lea r12,[rsp+0x8]                | 4c 8d 64 24 08
lea r12,[rsp+0xc8]               | 4c 8d a4 24 c8 00 00
lea r12,[rsp+0xf]                | 4c 8d 64 24 0f
lea r12d,[r10-0x30]              | 45 8d 62 d0
lea r12d,[r11+0x1]               | 45 8d 63 01
lea r12d,[r12+0x1]               | 45 8d 64 24 01
lea r12d,[r13-0x9]               | 45 8d 65 f7
lea r12d,[r14-0x2]               | 45 8d 66 fe
lea r12d,[r8+rsi*1]              | 45 8d 24 30
lea r12d,[rax+0x1]               | 44 8d 60 01
lea r12d,[rbp+0x1]               | 44 8d 65 01
lea r12d,[rbx+0x151]             | 44 8d a3 51 01 00 00
lea r12d,[rbx+0x1e]              | 44 8d 63 1e
lea r12d,[rbx-0x1]               | 44 8d 63 ff
lea r12d,[rcx+0x1]               | 44 8d 61 01
lea r12d,[rsi+0x1]               | 44 8d 66 01
lea r13,[r10+0x8]                | 4d 8d 6a 08
lea r13,[r11+0x17]               | 4d 8d 6b 17
lea r13,[r12+0x1]                | 4d 8d 6c 24 01
lea r13,[r12+0x2]                | 4d 8d 6c 24 02
lea r13,[r12+0x58]               | 4d 8d 6c 24 58
lea r13,[r12+0x5]                | 4d 8d 6c 24 05
lea r13,[r12+0x6]                | 4d 8d 6c 24 06
lea r13,[r12+0x800]              | 4d 8d ac 24 00 08 00
lea r13,[r12+0xc]                | 4d 8d 6c 24 0c
lea r13,[r12+rax*1]              | 4d 8d 2c 04
lea r13,[r12-0x1]                | 4d 8d 6c 24 ff
lea r13,[r14+0x1]                | 4d 8d 6e 01
lea r13,[r14+0x2]                | 4d 8d 6e 02
lea r13,[r14+r12*1]              | 4f 8d 2c 26
lea r13,[r14+rax*1]              | 4d 8d 2c 06
lea r13,[r15+0xd8]               | 4d 8d af d8 00 00 00
lea r13,[r9+0x20]                | 4d 8d 69 20
lea r13,[rax+0x1]                | 4c 8d 68 01
lea r13,[rax+r14*1]              | 4e 8d 2c 30
lea r13,[rax+r9*8-0x10]          | 4e 8d 6c c8 f0
lea r13,[rax+rbx*1-0x1]          | 4c 8d 6c 18 ff
lea r13,[rbp+0x1]                | 4c 8d 6d 01
lea r13,[rbp+0x3]                | 4c 8d 6d 03
lea r13,[rbp+0x5]                | 4c 8d 6d 05
lea r13,[rbp+0x8]                | 4c 8d 6d 08
lea r13,[rbp+r14*1+0x0]          | 4e 8d 6c 35 00
lea r13,[rbp-0x130]              | 4c 8d ad d0 fe ff ff
lea r13,[rbp-0x1c0]              | 4c 8d ad 40 fe ff ff
lea r13,[rbp-0x220]              | 4c 8d ad e0 fd ff ff
lea r13,[rbx+0x10]               | 4c 8d 6b 10
lea r13,[rbx+0x1]                | 4c 8d 6b 01
lea r13,[rbx+0x2]                | 4c 8d 6b 02
lea r13,[rbx+0x3]                | 4c 8d 6b 03
lea r13,[rbx+0x4]                | 4c 8d 6b 04
lea r13,[rbx+0x6]                | 4c 8d 6b 06
lea r13,[rbx+0x7]                | 4c 8d 6b 07
lea r13,[rbx+0x8]                | 4c 8d 6b 08
lea r13,[rbx+0xa0]               | 4c 8d ab a0 00 00 00
lea r13,[rbx+0xa]                | 4c 8d 6b 0a
lea r13,[rbx+rax*1]              | 4c 8d 2c 03
lea r13,[rbx-0x1]                | 4c 8d 6b ff
lea r13,[rcx+0x4e2600]           | 4c 8d a9 00 26 4e 00
lea r13,[rcx+rax*1]              | 4c 8d 2c 01
lea r13,[rcx+rsi*1]              | 4c 8d 2c 31
lea r13,[rdi+0x10]               | 4c 8d 6f 10
lea r13,[rdi+0x1]                | 4c 8d 6f 01
lea r13,[rdi+0x78]               | 4c 8d 6f 78
lea r13,[rdx+r8*1]               | 4e 8d 2c 02
lea r13,[rip+0x79a08]            | 4c 8d 2d 08 9a 07 00
lea r13,[rip+0x90f21]            | 4c 8d 2d 21 0f 09 00
lea r13,[rsi+r9*8+0x8]           | 4e 8d 6c ce 08
lea r13,[rsp+0x107]              | 4c 8d ac 24 07 01 00
lea r13,[rsp+0x10]               | 4c 8d 6c 24 10
lea r13,[rsp+0x170]              | 4c 8d ac 24 70 01 00
lea r13,[rsp+0x30]               | 4c 8d 6c 24 30
lea r13,[rsp+0x3e0]              | 4c 8d ac 24 e0 03 00
lea r13,[rsp+0x68]               | 4c 8d 6c 24 68
lea r13,[rsp+0x70]               | 4c 8d 6c 24 70
lea r13,[rsp+0xa0]               | 4c 8d ac 24 a0 00 00
lea r13,[rsp+0xc0]               | 4c 8d ac 24 c0 00 00
lea r13,[rsp+0xf]                | 4c 8d 6c 24 0f
lea r13d,[r10-0x1]               | 45 8d 6a ff
lea r13d,[r13+0x1]               | 45 8d 6d 01
lea r13d,[r14+0x1]               | 45 8d 6e 01
lea r13d,[r15+0x1]               | 45 8d 6f 01
lea r13d,[r9+0x1]                | 45 8d 69 01
lea r13d,[rcx+0x2]               | 44 8d 69 02
lea r13d,[rsi+rsi*1]             | 44 8d 2c 36
lea r13d,[rsi-0x1]               | 44 8d 6e ff
lea r14,[r11+0x14]               | 4d 8d 73 14
lea r14,[r11+0x1]                | 4d 8d 73 01
lea r14,[r11+r9*1-0x1]           | 4f 8d 74 0b ff
lea r14,[r12+r9*8]               | 4f 8d 34 cc
lea r14,[r12+rax*1+0x8]          | 4d 8d 74 04 08
lea r14,[r13+0x1]                | 4d 8d 75 01
lea r14,[r13+0x9]                | 4d 8d 75 09
lea r14,[r13+0xb]                | 4d 8d 75 0b
lea r14,[r13+0xd0]               | 4d 8d b5 d0 00 00 00
lea r14,[r13+r8*1+0x10]          | 4f 8d 74 05 10
lea r14,[r14+0x8]                | 4d 8d 76 08
lea r14,[r14+r15*1+0x1]          | 4f 8d 74 3e 01
lea r14,[r14+r15*8+0x8]          | 4f 8d 74 fe 08
lea r14,[r15+0x1]                | 4d 8d 77 01
lea r14,[r15+r13*1]              | 4f 8d 34 2f
lea r14,[r15+r9*1-0x9]           | 4f 8d 74 0f f7
lea r14,[r8+0x10]                | 4d 8d 70 10
lea r14,[r9+0x2]                 | 4d 8d 71 02
lea r14,[rax+0x15]               | 4c 8d 70 15
lea r14,[rax+0x1]                | 4c 8d 70 01
lea r14,[rax+0x48ad60]           | 4c 8d b0 60 ad 48 00
lea r14,[rax+r15*1-0x1]          | 4e 8d 74 38 ff
lea r14,[rax-0x1]                | 4c 8d 70 ff
lea r14,[rax-0x2]                | 4c 8d 70 fe
lea r14,[rbp+0x4]                | 4c 8d 75 04
lea r14,[rbp+rax*1-0x20]         | 4c 8d 74 05 e0
lea r14,[rbp-0x130]              | 4c 8d b5 d0 fe ff ff
lea r14,[rbp-0x220]              | 4c 8d b5 e0 fd ff ff
lea r14,[rbx+0x10]               | 4c 8d 73 10
lea r14,[rbx+0x11]               | 4c 8d 73 11
lea r14,[rbx+0x12]               | 4c 8d 73 12
lea r14,[rbx+0x14]               | 4c 8d 73 14
lea r14,[rbx+0x15]               | 4c 8d 73 15
lea r14,[rbx+0x17]               | 4c 8d 73 17
lea r14,[rbx+0x18]               | 4c 8d 73 18
lea r14,[rbx+0x19]               | 4c 8d 73 19
lea r14,[rbx+0x1b]               | 4c 8d 73 1b
lea r14,[rbx+0x1c]               | 4c 8d 73 1c
lea r14,[rbx+0x1d]               | 4c 8d 73 1d
lea r14,[rbx+0x2]                | 4c 8d 73 02
lea r14,[rbx+0x4]                | 4c 8d 73 04
lea r14,[rbx+0x580]              | 4c 8d b3 80 05 00 00
lea r14,[rbx+0x5]                | 4c 8d 73 05
lea r14,[rbx+0x6]                | 4c 8d 73 06
lea r14,[rbx+0x7]                | 4c 8d 73 07
lea r14,[rbx+0x8]                | 4c 8d 73 08
lea r14,[rbx+0x9]                | 4c 8d 73 09
lea r14,[rbx+0xa]                | 4c 8d 73 0a
lea r14,[rbx+0xc]                | 4c 8d 73 0c
lea r14,[rbx+0xd]                | 4c 8d 73 0d
lea r14,[rbx+0xe]                | 4c 8d 73 0e
lea r14,[rbx+0xf]                | 4c 8d 73 0f
lea r14,[rdi+0x10]               | 4c 8d 77 10
lea r14,[rdi+0x1]                | 4c 8d 77 01
lea r14,[rdi+0x5]                | 4c 8d 77 05
lea r14,[rdx+0x10]               | 4c 8d 72 10
lea r14,[rdx+0x15b0]             | 4c 8d b2 b0 15 00 00
lea r14,[rip+0x77a58]            | 4c 8d 35 58 7a 07 00
lea r14,[rip+0x77c7a]            | 4c 8d 35 7a 7c 07 00
lea r14,[rip+0x785d3]            | 4c 8d 35 d3 85 07 00
lea r14,[rip+0x7d51e]            | 4c 8d 35 1e d5 07 00
lea r14,[rip+0x7fad1]            | 4c 8d 35 d1 fa 07 00
lea r14,[rip+0x8cbc5]            | 4c 8d 35 c5 cb 08 00
lea r14,[rip+0x8f2a5]            | 4c 8d 35 a5 f2 08 00
lea r14,[rsi+r14*1+0x1]          | 4e 8d 74 36 01
lea r14,[rsi+rax*1+0x64]         | 4c 8d 74 06 64
lea r14,[rsi+rbx*1-0x10]         | 4c 8d 74 1e f0
lea r14,[rsp+0x10]               | 4c 8d 74 24 10
lea r14,[rsp+0x17]               | 4c 8d 74 24 17
lea r14,[rsp+0x1f]               | 4c 8d 74 24 1f
lea r14,[rsp+0x60]               | 4c 8d 74 24 60
lea r14,[rsp+0x88]               | 4c 8d b4 24 88 00 00
lea r14,[rsp+0xf]                | 4c 8d 74 24 0f
lea r14d,[r12+0x1]               | 45 8d 74 24 01
lea r14d,[r12-0x1]               | 45 8d 74 24 ff
lea r14d,[r14+rbx*1-0x1]         | 45 8d 74 1e ff
lea r14d,[r14+rdx*1+0x4]         | 45 8d 74 16 04
lea r14d,[r14-0x1]               | 45 8d 76 ff
lea r14d,[r14-0x2]               | 45 8d 76 fe
lea r14d,[r15+r12*1]             | 47 8d 34 27
lea r14d,[r15-0x1]               | 45 8d 77 ff
lea r14d,[r8+0x2]                | 45 8d 70 02
lea r14d,[r9-0x3]                | 45 8d 71 fd
lea r14d,[rax+0x1]               | 44 8d 70 01
lea r14d,[rax-0x1]               | 44 8d 70 ff
lea r14d,[rbp+0x1]               | 44 8d 75 01
lea r14d,[rbx+0x1]               | 44 8d 73 01
lea r14d,[rdx+0x4]               | 44 8d 72 04
lea r14d,[rsi+0x1]               | 44 8d 76 01
lea r15,[r10+0x4]                | 4d 8d 7a 04
lea r15,[r11+0x1]                | 4d 8d 7b 01
lea r15,[r12+0x1]                | 4d 8d 7c 24 01
lea r15,[r12+r14*1-0x10]         | 4f 8d 7c 34 f0
lea r15,[r13+0x1]                | 4d 8d 7d 01
lea r15,[r13+0x2]                | 4d 8d 7d 02
lea r15,[r14+0x8]                | 4d 8d 7e 08
lea r15,[r15+0x1]                | 4d 8d 7f 01
lea r15,[r15+rsi*1]              | 4d 8d 3c 37
lea r15,[r8+rax*1+0x1]           | 4d 8d 7c 00 01
lea r15,[r9+0x1]                 | 4d 8d 79 01
lea r15,[r9+0x48baa0]            | 4d 8d b9 a0 ba 48 00
lea r15,[rax+0x1]                | 4c 8d 78 01
lea r15,[rbp+0x2]                | 4c 8d 7d 02
lea r15,[rbp-0x3a0]              | 4c 8d bd 60 fc ff ff
lea r15,[rbx+0x20]               | 4c 8d 7b 20
lea r15,[rbx+0x2]                | 4c 8d 7b 02
lea r15,[rbx+0x3]                | 4c 8d 7b 03
lea r15,[rbx+0x40]               | 4c 8d 7b 40
lea r15,[rbx+0x5]                | 4c 8d 7b 05
lea r15,[rbx+0x60]               | 4c 8d 7b 60
lea r15,[rbx+0x7]                | 4c 8d 7b 07
lea r15,[rbx+0x80]               | 4c 8d bb 80 00 00 00
lea r15,[rbx+0x8]                | 4c 8d 7b 08
lea r15,[rbx+0xc]                | 4c 8d 7b 0c
lea r15,[rbx+0xd]                | 4c 8d 7b 0d
lea r15,[rbx+r11*1]              | 4e 8d 3c 1b
lea r15,[rcx+0x1]                | 4c 8d 79 01
lea r15,[rcx+rsi*1-0x8]          | 4c 8d 7c 31 f8
lea r15,[rdi+0x1]                | 4c 8d 7f 01
lea r15,[rdx+0x1278]             | 4c 8d ba 78 12 00 00
lea r15,[rdx+0x1]                | 4c 8d 7a 01
lea r15,[rsi+0x1]                | 4c 8d 7e 01
lea r15,[rsp+0x150]              | 4c 8d bc 24 50 01 00
lea r15,[rsp+0x1c]               | 4c 8d 7c 24 1c
lea r15,[rsp+0x30]               | 4c 8d 7c 24 30
lea r15,[rsp+0x58]               | 4c 8d 7c 24 58
lea r15,[rsp+0x70]               | 4c 8d 7c 24 70
lea r15,[rsp+0xa0]               | 4c 8d bc 24 a0 00 00
lea r15,[rsp+0xc0]               | 4c 8d bc 24 c0 00 00
lea r15,[rsp+0xf]                | 4c 8d 7c 24 0f
lea r15d,[r14-0x1]               | 45 8d 7e ff
lea r15d,[r9*4+0x0]              | 46 8d 3c 8d 00 00 00
lea r15d,[rbp+0x1e]              | 44 8d 7d 1e
lea r15d,[rbp-0x1]               | 44 8d 7d ff
lea r15d,[rcx+rcx*1]             | 44 8d 3c 09
lea r15d,[rdx+0x1]               | 44 8d 7a 01
lea r8,[r10+0x1]                 | 4d 8d 42 01
lea r8,[r11+0x17]                | 4d 8d 43 17
lea r8,[r11+0x8]                 | 4d 8d 43 08
lea r8,[r11+rbp*1]               | 4d 8d 04 2b
lea r8,[r12+0x1]                 | 4d 8d 44 24 01
lea r8,[r12+0x78]                | 4d 8d 44 24 78
lea r8,[r12+0xc]                 | 4d 8d 44 24 0c
lea r8,[r12+r13*1]               | 4f 8d 04 2c
lea r8,[r13+r12*1+0x0]           | 4f 8d 44 25 00
lea r8,[r13+r13*2+0x0]           | 4f 8d 44 6d 00
lea r8,[r13-0x8]                 | 4d 8d 45 f8
lea r8,[r14*8+0x0]               | 4e 8d 04 f5 00 00 00
lea r8,[r14+rbx*1]               | 4d 8d 04 1e
lea r8,[r15+0x38]                | 4d 8d 47 38
lea r8,[r15+0x8]                 | 4d 8d 47 08
lea r8,[r15+r12*1]               | 4f 8d 04 27
lea r8,[r15+rcx*8]               | 4d 8d 04 cf
lea r8,[r15+rdi*8]               | 4d 8d 04 ff
lea r8,[r8+0x4]                  | 4d 8d 40 04
lea r8,[rax+0x18]                | 4c 8d 40 18
lea r8,[rax+0x1]                 | 4c 8d 40 01
lea r8,[rax+0x3]                 | 4c 8d 40 03
lea r8,[rax+0x80]                | 4c 8d 80 80 00 00 00
lea r8,[rax+0x8]                 | 4c 8d 40 08
lea r8,[rax+rdi*1-0x1]           | 4c 8d 44 38 ff
lea r8,[rax+rdi*1]               | 4c 8d 04 38
lea r8,[rax-0x1]                 | 4c 8d 40 ff
lea r8,[rbp+0x18]                | 4c 8d 45 18
lea r8,[rbp+0x1]                 | 4c 8d 45 01
lea r8,[rbp+0x580]               | 4c 8d 85 80 05 00 00
lea r8,[rbp+0xb10]               | 4c 8d 85 10 0b 00 00
lea r8,[rbp+0xb20]               | 4c 8d 85 20 0b 00 00
lea r8,[rbp+0xb30]               | 4c 8d 85 30 0b 00 00
lea r8,[rbp+0xb40]               | 4c 8d 85 40 0b 00 00
lea r8,[rbp-0xc0]                | 4c 8d 85 40 ff ff ff
lea r8,[rbx*8+0x0]               | 4c 8d 04 dd 00 00 00
lea r8,[rbx+0x58]                | 4c 8d 43 58
lea r8,[rbx+rbx*2]               | 4c 8d 04 5b
lea r8,[rcx*4+0x17]              | 4c 8d 04 8d 17 00 00
lea r8,[rcx+0x1]                 | 4c 8d 41 01
lea r8,[rcx+rax*1]               | 4c 8d 04 01
lea r8,[rcx+rsi*1-0x18]          | 4c 8d 44 31 e8
lea r8,[rdi+0x1]                 | 4c 8d 47 01
lea r8,[rdi+0x40]                | 4c 8d 47 40
lea r8,[rdi+0x7]                 | 4c 8d 47 07
lea r8,[rdi+0x8]                 | 4c 8d 47 08
lea r8,[rdi+r9*1+0x1]            | 4e 8d 44 0f 01
lea r8,[rdx*8+0xf]               | 4c 8d 04 d5 0f 00 00
lea r8,[rdx+0x18]                | 4c 8d 42 18
lea r8,[rdx+0x1]                 | 4c 8d 42 01
lea r8,[rdx+rax*1+0x3]           | 4c 8d 44 02 03
lea r8,[rdx+rcx*1]               | 4c 8d 04 0a
lea r8,[rip+0x76f38]             | 4c 8d 05 38 6f 07 00
lea r8,[rip+0x77108]             | 4c 8d 05 08 71 07 00
lea r8,[rip+0xa22da]             | 4c 8d 05 da 22 0a 00
lea r8,[rsi*8+0x17]              | 4c 8d 04 f5 17 00 00
lea r8,[rsi+0x10]                | 4c 8d 46 10
lea r8,[rsi+0x1]                 | 4c 8d 46 01
lea r8,[rsi+rax*1+0x1]           | 4c 8d 44 06 01
lea r8,[rsi+rsi*2]               | 4c 8d 04 76
lea r8,[rsp+0x108]               | 4c 8d 84 24 08 01 00
lea r8,[rsp+0x10]                | 4c 8d 44 24 10
lea r8,[rsp+0x110]               | 4c 8d 84 24 10 01 00
lea r8,[rsp+0x118]               | 4c 8d 84 24 18 01 00
lea r8,[rsp+0x20]                | 4c 8d 44 24 20
lea r8,[rsp+0x210]               | 4c 8d 84 24 10 02 00
lea r8,[rsp+0x268]               | 4c 8d 84 24 68 02 00
lea r8,[rsp+0x288]               | 4c 8d 84 24 88 02 00
lea r8,[rsp+0x290]               | 4c 8d 84 24 90 02 00
lea r8,[rsp+0x30]                | 4c 8d 44 24 30
lea r8,[rsp+0x330]               | 4c 8d 84 24 30 03 00
lea r8,[rsp+0x40]                | 4c 8d 44 24 40
lea r8,[rsp+0x50]                | 4c 8d 44 24 50
lea r8,[rsp+0x58]                | 4c 8d 44 24 58
lea r8,[rsp+0x68]                | 4c 8d 44 24 68
lea r8,[rsp+0x78]                | 4c 8d 44 24 78
lea r8,[rsp+0x7]                 | 4c 8d 44 24 07
lea r8,[rsp+0x8]                 | 4c 8d 44 24 08
lea r8,[rsp+0xc0]                | 4c 8d 84 24 c0 00 00
lea r8,[rsp+0xe8]                | 4c 8d 84 24 e8 00 00
lea r8,[rsp+0xf]                 | 4c 8d 44 24 0f
lea r8,[rsp+r12*1+0x4b0]         | 4e 8d 84 24 b0 04 00
lea r8d,[r10+0x1]                | 45 8d 42 01
lea r8d,[r10+r11*1]              | 47 8d 04 1a
lea r8d,[r11+0x1]                | 45 8d 43 01
lea r8d,[r11+0x2]                | 45 8d 43 02
lea r8d,[r11+0x8]                | 45 8d 43 08
lea r8d,[r11+r9*4+0x1]           | 47 8d 44 8b 01
lea r8d,[r13+r13*1+0x1]          | 47 8d 44 2d 01
lea r8d,[r14+0x1]                | 45 8d 46 01
lea r8d,[r14+r11*8+0x3]          | 47 8d 44 de 03
lea r8d,[r14-0x1]                | 45 8d 46 ff
lea r8d,[r14-0x3]                | 45 8d 46 fd
lea r8d,[r15+0x3]                | 45 8d 47 03
lea r8d,[r9+0x2]                 | 45 8d 41 02
lea r8d,[rax*8+0x0]              | 44 8d 04 c5 00 00 00
lea r8d,[rax+0x1]                | 44 8d 40 01
lea r8d,[rax+0x4]                | 44 8d 40 04
lea r8d,[rax+rdx*1-0x1]          | 44 8d 44 10 ff
lea r8d,[rax+rdx*2]              | 44 8d 04 50
lea r8d,[rax-0x1]                | 44 8d 40 ff
lea r8d,[rbx+0x1000]             | 44 8d 83 00 10 00 00
lea r8d,[rbx+0x1]                | 44 8d 43 01
lea r8d,[rcx-0x1c]               | 44 8d 41 e4
lea r8d,[rcx-0x41]               | 44 8d 41 bf
lea r8d,[rcx-0xd]                | 44 8d 41 f3
lea r8d,[rcx-0xf]                | 44 8d 41 f1
lea r8d,[rdi+0x6]                | 44 8d 47 06
lea r8d,[rdi-0x4ef]              | 44 8d 87 11 fb ff ff
lea r8d,[rdx+rax*1]              | 44 8d 04 02
lea r8d,[rdx+rdx*1+0x2]          | 44 8d 44 12 02
lea r8d,[rdx-0x30]               | 44 8d 42 d0
lea r9,[r10*8+0x0]               | 4e 8d 0c d5 00 00 00
lea r9,[r11+r11*2]               | 4f 8d 0c 5b
lea r9,[r12+0x3]                 | 4d 8d 4c 24 03
lea r9,[r12+r14*1]               | 4f 8d 0c 34
lea r9,[r12+rdi*1]               | 4d 8d 0c 3c
lea r9,[r13*4+0x0]               | 4e 8d 0c ad 00 00 00
lea r9,[r13+0x1]                 | 4d 8d 4d 01
lea r9,[r13+rcx*1+0x0]           | 4d 8d 4c 0d 00
lea r9,[r13+rdi*8+0x0]           | 4d 8d 4c fd 00
lea r9,[r14+0x17]                | 4d 8d 4e 17
lea r9,[r14+0x2]                 | 4d 8d 4e 02
lea r9,[r14+rbx*1]               | 4d 8d 0c 1e
lea r9,[r15*4+0x0]               | 4e 8d 0c bd 00 00 00
lea r9,[r15+rax*8]               | 4d 8d 0c c7
lea r9,[r8+0x10]                 | 4d 8d 48 10
lea r9,[r8+0x1]                  | 4d 8d 48 01
lea r9,[r8+0x48baa0]             | 4d 8d 88 a0 ba 48 00
lea r9,[r8+0x4]                  | 4d 8d 48 04
lea r9,[r8+0x6]                  | 4d 8d 48 06
lea r9,[r8-0x18]                 | 4d 8d 48 e8
lea r9,[r8-0x1]                  | 4d 8d 48 ff
lea r9,[rax+0x1]                 | 4c 8d 48 01
lea r9,[rax+0x3]                 | 4c 8d 48 03
lea r9,[rax+0x8]                 | 4c 8d 48 08
lea r9,[rax+r12*1+0x10]          | 4e 8d 4c 20 10
lea r9,[rax+rax*2]               | 4c 8d 0c 40
lea r9,[rax+rsi*1]               | 4c 8d 0c 30
lea r9,[rbp+0x20]                | 4c 8d 4d 20
lea r9,[rbp+0x4e6030]            | 4c 8d 8d 30 60 4e 00
lea r9,[rbx+0x1]                 | 4c 8d 4b 01
lea r9,[rbx+0x2]                 | 4c 8d 4b 02
lea r9,[rbx+0x4]                 | 4c 8d 4b 04
lea r9,[rbx+0x60]                | 4c 8d 4b 60
lea r9,[rbx+rdx*1+0xc]           | 4c 8d 4c 13 0c
lea r9,[rcx+r12*1+0x8]           | 4e 8d 4c 21 08
lea r9,[rcx+r8*1+0x8]            | 4e 8d 4c 01 08
lea r9,[rcx+rdi*1+0x8]           | 4c 8d 4c 39 08
lea r9,[rdi+0x10]                | 4c 8d 4f 10
lea r9,[rdi+0x2]                 | 4c 8d 4f 02
lea r9,[rdi+0x3]                 | 4c 8d 4f 03
lea r9,[rdi+r8*1]                | 4e 8d 0c 07
lea r9,[rdi+r8*8]                | 4e 8d 0c c7
lea r9,[rdi+rbx*1]               | 4c 8d 0c 1f
lea r9,[rdx+0x1]                 | 4c 8d 4a 01
lea r9,[rdx+0x2]                 | 4c 8d 4a 02
lea r9,[rdx+0x8]                 | 4c 8d 4a 08
lea r9,[rip+0x8bad0]             | 4c 8d 0d d0 ba 08 00
lea r9,[rip+0x9417e]             | 4c 8d 0d 7e 41 09 00
lea r9,[rsi+0x1]                 | 4c 8d 4e 01
lea r9,[rsi+0x8]                 | 4c 8d 4e 08
lea r9,[rsi+rsi*2]               | 4c 8d 0c 76
lea r9,[rsp+0x100]               | 4c 8d 8c 24 00 01 00
lea r9,[rsp+0x10]                | 4c 8d 4c 24 10
lea r9,[rsp+0x118]               | 4c 8d 8c 24 18 01 00
lea r9,[rsp+0x128]               | 4c 8d 8c 24 28 01 00
lea r9,[rsp+0x140]               | 4c 8d 8c 24 40 01 00
lea r9,[rsp+0x150]               | 4c 8d 8c 24 50 01 00
lea r9,[rsp+0x278]               | 4c 8d 8c 24 78 02 00
lea r9,[rsp+0x288]               | 4c 8d 8c 24 88 02 00
lea r9,[rsp+0x330]               | 4c 8d 8c 24 30 03 00
lea r9,[rsp+0x34]                | 4c 8d 4c 24 34
lea r9,[rsp+0x420]               | 4c 8d 8c 24 20 04 00
lea r9,[rsp+0x48]                | 4c 8d 4c 24 48
lea r9,[rsp+0x50]                | 4c 8d 4c 24 50
lea r9,[rsp+0x60]                | 4c 8d 4c 24 60
lea r9,[rsp+0x68]                | 4c 8d 4c 24 68
lea r9,[rsp+0x70]                | 4c 8d 4c 24 70
lea r9,[rsp+0xb0]                | 4c 8d 8c 24 b0 00 00
lea r9,[rsp+0xc0]                | 4c 8d 8c 24 c0 00 00
lea r9,[rsp+0xc8]                | 4c 8d 8c 24 c8 00 00
lea r9d,[r10-0x3]                | 45 8d 4a fd
lea r9d,[r10-0xc]                | 45 8d 4a f4
lea r9d,[r11+0x2]                | 45 8d 4b 02
lea r9d,[r12-0x1]                | 45 8d 4c 24 ff
lea r9d,[r13+0x1]                | 45 8d 4d 01
lea r9d,[r13+rax*1+0x0]          | 45 8d 4c 05 00
lea r9d,[r14+0x2]                | 45 8d 4e 02
lea r9d,[r14-0x3]                | 45 8d 4e fd
lea r9d,[r14-0x4]                | 45 8d 4e fc
lea r9d,[r15+0x1]                | 45 8d 4f 01
lea r9d,[r8+0x1]                 | 45 8d 48 01
lea r9d,[r8+0x2]                 | 45 8d 48 02
lea r9d,[r8-0x6]                 | 45 8d 48 fa
lea r9d,[rax+0x1]                | 44 8d 48 01
lea r9d,[rax+r8*1]               | 46 8d 0c 00
lea r9d,[rbx-0x1]                | 44 8d 4b ff
lea r9d,[rcx-0x41]               | 44 8d 49 bf
lea r9d,[rdi-0x1]                | 44 8d 4f ff
lea r9d,[rsi+0x2]                | 44 8d 4e 02
lea r9d,[rsi-0x29]               | 44 8d 4e d7
lea rax,[r10+r10*2]              | 4b 8d 04 52
lea rax,[r11+0x17]               | 49 8d 43 17
lea rax,[r11+0x8]                | 49 8d 43 08
lea rax,[r11+rcx*1]              | 49 8d 04 0b
lea rax,[r12+0x10]               | 49 8d 44 24 10
lea rax,[r12+0x1]                | 49 8d 44 24 01
lea rax,[r12+0x28]               | 49 8d 44 24 28
lea rax,[r12+0x8]                | 49 8d 44 24 08
lea rax,[r12-0x1]                | 49 8d 44 24 ff
lea rax,[r13+0x5]                | 49 8d 45 05
lea rax,[r14+0x1]                | 49 8d 46 01
lea rax,[r14+r8*1]               | 4b 8d 04 06
lea rax,[r14+rax*1+0x1]          | 49 8d 44 06 01
lea rax,[r14+rbx*1]              | 49 8d 04 1e
lea rax,[r15+0x1]                | 49 8d 47 01
lea rax,[r15+0x2]                | 49 8d 47 02
lea rax,[r15+0xd]                | 49 8d 47 0d
lea rax,[r8+0x1]                 | 49 8d 40 01
lea rax,[r8+0x2]                 | 49 8d 40 02
lea rax,[r8+0x3]                 | 49 8d 40 03
lea rax,[r8+0x4]                 | 49 8d 40 04
lea rax,[r8+0x7]                 | 49 8d 40 07
lea rax,[r8+0x8]                 | 49 8d 40 08
lea rax,[r8+r8*2]                | 4b 8d 04 40
lea rax,[r8+r9*1]                | 4b 8d 04 08
lea rax,[r9+0x2]                 | 49 8d 41 02
lea rax,[rax*8+0xf]              | 48 8d 04 c5 0f 00 00
lea rax,[rax+0x1]                | 48 8d 40 01
lea rax,[rax+rax*4]              | 48 8d 04 80
lea rax,[rax+rcx*1+0x3]          | 48 8d 44 08 03
lea rax,[rax+rdx*1-0x1]          | 48 8d 44 10 ff
lea rax,[rbp+0x10]               | 48 8d 45 10
lea rax,[rbp+0x18]               | 48 8d 45 18
lea rax,[rbp+0x1]                | 48 8d 45 01
lea rax,[rbp+0x58]               | 48 8d 45 58
lea rax,[rbp+rax*1-0x1]          | 48 8d 44 05 ff
lea rax,[rbp+rbp*2+0x0]          | 48 8d 44 6d 00
lea rax,[rbp+rbp*4+0x0]          | 48 8d 44 ad 00
lea rax,[rbp+rcx*8+0x0]          | 48 8d 44 cd 00
lea rax,[rbp-0x824]              | 48 8d 85 dc f7 ff ff
lea rax,[rbp-0xc0]               | 48 8d 85 40 ff ff ff
lea rax,[rbx+0x10]               | 48 8d 43 10
lea rax,[rbx+0x1]                | 48 8d 43 01
lea rax,[rbx+0x20]               | 48 8d 43 20
lea rax,[rbx+0x2]                | 48 8d 43 02
lea rax,[rbx+0x39]               | 48 8d 43 39
lea rax,[rbx+0x58]               | 48 8d 43 58
lea rax,[rbx+0x70]               | 48 8d 43 70
lea rax,[rbx+0x80]               | 48 8d 83 80 00 00 00
lea rax,[rbx+rax*1+0x1]          | 48 8d 44 03 01
lea rax,[rbx+rsi*1]              | 48 8d 04 33
lea rax,[rbx-0x20]               | 48 8d 43 e0
lea rax,[rbx-0x80]               | 48 8d 43 80
lea rax,[rcx+0x1]                | 48 8d 41 01
lea rax,[rcx+0x2]                | 48 8d 41 02
lea rax,[rcx+r8*1-0x20]          | 4a 8d 44 01 e0
lea rax,[rcx+r8*1]               | 4a 8d 04 01
lea rax,[rcx+rax*1+0x10]         | 48 8d 44 01 10
lea rax,[rcx+rsi*1]              | 48 8d 04 31
lea rax,[rdi+0x10]               | 48 8d 47 10
lea rax,[rdi+0x18]               | 48 8d 47 18
lea rax,[rdi+0x1]                | 48 8d 47 01
lea rax,[rdi+0x8]                | 48 8d 47 08
lea rax,[rdi+r15*1]              | 4a 8d 04 3f
lea rax,[rdi-0x18]               | 48 8d 47 e8
lea rax,[rdx+0x1000]             | 48 8d 82 00 10 00 00
lea rax,[rdx+0x1]                | 48 8d 42 01
lea rax,[rdx+0x2]                | 48 8d 42 02
lea rax,[rdx+0x3]                | 48 8d 42 03
lea rax,[rdx+0x5]                | 48 8d 42 05
lea rax,[rdx+0x70]               | 48 8d 42 70
lea rax,[rdx+0x7]                | 48 8d 42 07
lea rax,[rdx+0x8]                | 48 8d 42 08
lea rax,[rdx+r14*1]              | 4a 8d 04 32
lea rax,[rdx+rax*1-0x1]          | 48 8d 44 02 ff
lea rax,[rdx+rdi*1+0x3]          | 48 8d 44 3a 03
lea rax,[rdx+rdx*1]              | 48 8d 04 12
lea rax,[rdx+rdx*2]              | 48 8d 04 52
lea rax,[rip+0x71f71]            | 48 8d 05 71 1f 07 00
lea rax,[rip+0x747b4]            | 48 8d 05 b4 47 07 00
lea rax,[rip+0x7e8cb]            | 48 8d 05 cb e8 07 00
lea rax,[rip+0x81058]            | 48 8d 05 58 10 08 00
lea rax,[rip+0x881cd]            | 48 8d 05 cd 81 08 00
lea rax,[rip+0x881ce]            | 48 8d 05 ce 81 08 00
lea rax,[rip+0x886bd]            | 48 8d 05 bd 86 08 00
lea rax,[rip+0x886d5]            | 48 8d 05 d5 86 08 00
lea rax,[rip+0x88939]            | 48 8d 05 39 89 08 00
lea rax,[rip+0x8896e]            | 48 8d 05 6e 89 08 00
lea rax,[rip+0x8898a]            | 48 8d 05 8a 89 08 00
lea rax,[rip+0x889bd]            | 48 8d 05 bd 89 08 00
lea rax,[rip+0x8c72d]            | 48 8d 05 2d c7 08 00
lea rax,[rip+0x8cb6b]            | 48 8d 05 6b cb 08 00
lea rax,[rip+0x8ef99]            | 48 8d 05 99 ef 08 00
lea rax,[rip+0x90595]            | 48 8d 05 95 05 09 00
lea rax,[rip+0x92209]            | 48 8d 05 09 22 09 00
lea rax,[rip+0x95037]            | 48 8d 05 37 50 09 00
lea rax,[rip+0x95086]            | 48 8d 05 86 50 09 00
lea rax,[rip+0x950a3]            | 48 8d 05 a3 50 09 00
lea rax,[rip+0x950c0]            | 48 8d 05 c0 50 09 00
lea rax,[rip+0x9512c]            | 48 8d 05 2c 51 09 00
lea rax,[rip+0x95149]            | 48 8d 05 49 51 09 00
lea rax,[rip+0x951b2]            | 48 8d 05 b2 51 09 00
lea rax,[rip+0x95255]            | 48 8d 05 55 52 09 00
lea rax,[rip+0xd4ae]             | 48 8d 05 ae d4 00 00
lea rax,[rip+0xffffffffffffedfd] | 48 8d 05 fd ed ff ff
lea rax,[rip+0xfffffffffffffc8e] | 48 8d 05 8e fc ff ff
lea rax,[rip+0xfffffffffffffec7] | 48 8d 05 c7 fe ff ff
lea rax,[rip+0xffffffffffffff82] | 48 8d 05 82 ff ff ff
lea rax,[rsi+0x10]               | 48 8d 46 10
lea rax,[rsi+0x1]                | 48 8d 46 01
lea rax,[rsi+0x2]                | 48 8d 46 02
lea rax,[rsi+0x4]                | 48 8d 46 04
lea rax,[rsi+0x8]                | 48 8d 46 08
lea rax,[rsi+rax*1+0x1]          | 48 8d 44 06 01
lea rax,[rsi+rcx*1]              | 48 8d 04 0e
lea rax,[rsi+rdx*1]              | 48 8d 04 16
lea rax,[rsi+rsi*1]              | 48 8d 04 36
lea rax,[rsp+0x100]              | 48 8d 84 24 00 01 00
lea rax,[rsp+0x10]               | 48 8d 44 24 10
lea rax,[rsp+0x110]              | 48 8d 84 24 10 01 00
lea rax,[rsp+0x120]              | 48 8d 84 24 20 01 00
lea rax,[rsp+0x190]              | 48 8d 84 24 90 01 00
lea rax,[rsp+0x1a8]              | 48 8d 84 24 a8 01 00
lea rax,[rsp+0x1c8]              | 48 8d 84 24 c8 01 00
lea rax,[rsp+0x20]               | 48 8d 44 24 20
lea rax,[rsp+0x278]              | 48 8d 84 24 78 02 00
lea rax,[rsp+0x38]               | 48 8d 44 24 38
lea rax,[rsp+0x40]               | 48 8d 44 24 40
lea rax,[rsp+0x48]               | 48 8d 44 24 48
lea rax,[rsp+0x4b0]              | 48 8d 84 24 b0 04 00
lea rax,[rsp+0x70]               | 48 8d 44 24 70
lea rax,[rsp+0x810]              | 48 8d 84 24 10 08 00
lea rax,[rsp+0x8]                | 48 8d 44 24 08
lea rax,[rsp+0xe0]               | 48 8d 84 24 e0 00 00
lea rax,[rsp+0xf]                | 48 8d 44 24 0f
lea rbp,[r11+0x1]                | 49 8d 6b 01
lea rbp,[r11+0x21]               | 49 8d 6b 21
lea rbp,[r11+0x4cb760]           | 49 8d ab 60 b7 4c 00
lea rbp,[r12+0x1]                | 49 8d 6c 24 01
lea rbp,[r12+r15*1]              | 4b 8d 2c 3c
lea rbp,[r13+0x1]                | 49 8d 6d 01
lea rbp,[r13+rax*1+0x0]          | 49 8d 6c 05 00
lea rbp,[r13+rdx*1+0x8]          | 49 8d 6c 15 08
lea rbp,[r14+0x1]                | 49 8d 6e 01
lea rbp,[r14+0x3]                | 49 8d 6e 03
lea rbp,[r8+0x1]                 | 49 8d 68 01
lea rbp,[r8+0x48baa0]            | 49 8d a8 a0 ba 48 00
lea rbp,[r8+0x8]                 | 49 8d 68 08
lea rbp,[r9+0x14]                | 49 8d 69 14
lea rbp,[r9+0x58]                | 49 8d 69 58
lea rbp,[r9+0xa]                 | 49 8d 69 0a
lea rbp,[rax*8+0x10]             | 48 8d 2c c5 10 00 00
lea rbp,[rax+0x18]               | 48 8d 68 18
lea rbp,[rax+0x1]                | 48 8d 68 01
lea rbp,[rax+0x60]               | 48 8d 68 60
lea rbp,[rax+r14*1+0x2]          | 4a 8d 6c 30 02
lea rbp,[rbp+0x1]                | 48 8d 6d 01
lea rbp,[rbp+0x25]               | 48 8d 6d 25
lea rbp,[rbp+rbx*1-0x8]          | 48 8d 6c 1d f8
lea rbp,[rbx+0x1]                | 48 8d 6b 01
lea rbp,[rbx+rcx*1]              | 48 8d 2c 0b
lea rbp,[rbx-0xc]                | 48 8d 6b f4
lea rbp,[rcx+0x10000]            | 48 8d a9 00 00 01 00
lea rbp,[rcx-0x1]                | 48 8d 69 ff
lea rbp,[rdi+0x10]               | 48 8d 6f 10
lea rbp,[rdi+0x50]               | 48 8d 6f 50
lea rbp,[rdi+0x80]               | 48 8d af 80 00 00 00
lea rbp,[rdi+r15*1]              | 4a 8d 2c 3f
lea rbp,[rdi+rsi*1]              | 48 8d 2c 37
lea rbp,[rip+0x784dc]            | 48 8d 2d dc 84 07 00
lea rbp,[rip+0x78b3a]            | 48 8d 2d 3a 8b 07 00
lea rbp,[rip+0x78be6]            | 48 8d 2d e6 8b 07 00
lea rbp,[rip+0x78d21]            | 48 8d 2d 21 8d 07 00
lea rbp,[rip+0x7979d]            | 48 8d 2d 9d 97 07 00
lea rbp,[rip+0x81622]            | 48 8d 2d 22 16 08 00
lea rbp,[rip+0x9057c]            | 48 8d 2d 7c 05 09 00
lea rbp,[rip+0xa21e3]            | 48 8d 2d e3 21 0a 00
lea rbp,[rip+0xa22f8]            | 48 8d 2d f8 22 0a 00
lea rbp,[rip+0xffffffffffffe7f4] | 48 8d 2d f4 e7 ff ff
lea rbp,[rip+0xffffffffffffeec5] | 48 8d 2d c5 ee ff ff
lea rbp,[rsi+0x1]                | 48 8d 6e 01
lea rbp,[rsi+0x20]               | 48 8d 6e 20
lea rbp,[rsi+rax*1]              | 48 8d 2c 06
lea rbp,[rsi+rdx*1]              | 48 8d 2c 16
lea rbp,[rsp+0x10]               | 48 8d 6c 24 10
lea rbp,[rsp+0x17]               | 48 8d 6c 24 17
lea rbp,[rsp+0x248]              | 48 8d ac 24 48 02 00
lea rbp,[rsp+0x2c0]              | 48 8d ac 24 c0 02 00
lea rbp,[rsp+0x360]              | 48 8d ac 24 60 03 00
lea rbp,[rsp+0x3e0]              | 48 8d ac 24 e0 03 00
lea rbp,[rsp+0x40]               | 48 8d 6c 24 40
lea rbp,[rsp+0x4]                | 48 8d 6c 24 04
lea rbp,[rsp+0x5d0]              | 48 8d ac 24 d0 05 00
lea rbp,[rsp+0x8]                | 48 8d 6c 24 08
lea rbp,[rsp+0x90]               | 48 8d ac 24 90 00 00
lea rbp,[rsp+0x98]               | 48 8d ac 24 98 00 00
lea rbx,[r11+0x17]               | 49 8d 5b 17
lea rbx,[r11+r14*1+0x8]          | 4b 8d 5c 33 08
lea rbx,[r12+0x1]                | 49 8d 5c 24 01
lea rbx,[r12+rax*1]              | 49 8d 1c 04
lea rbx,[r13+0x1]                | 49 8d 5d 01
lea rbx,[r13+0x20]               | 49 8d 5d 20
lea rbx,[r13+r13*4+0x5]          | 4b 8d 5c ad 05
lea rbx,[r13+r8*8+0x0]           | 4b 8d 5c c5 00
lea rbx,[r13-0xc]                | 49 8d 5d f4
lea rbx,[r14+0x1]                | 49 8d 5e 01
lea rbx,[r15-0x1]                | 49 8d 5f ff
lea rbx,[r8+rbx*1]               | 49 8d 1c 18
lea rbx,[r9+r14*1+0x8]           | 4b 8d 5c 31 08
lea rbx,[r9+rdx*1]               | 49 8d 1c 11
lea rbx,[rax+0x18]               | 48 8d 58 18
lea rbx,[rax+0x1]                | 48 8d 58 01
lea rbx,[rax+0x2]                | 48 8d 58 02
lea rbx,[rax+rdx*1-0x1]          | 48 8d 5c 10 ff
lea rbx,[rbp+0xa]                | 48 8d 5d 0a
lea rbx,[rbp-0x160]              | 48 8d 9d a0 fe ff ff
lea rbx,[rbp-0x2a0]              | 48 8d 9d 60 fd ff ff
lea rbx,[rbx*4+0x4]              | 48 8d 1c 9d 04 00 00
lea rbx,[rbx+0x1]                | 48 8d 5b 01
lea rbx,[rbx+rax*1+0x1]          | 48 8d 5c 03 01
lea rbx,[rbx+rbp*1+0x4]          | 48 8d 5c 2b 04
lea rbx,[rcx+0x1]                | 48 8d 59 01
lea rbx,[rcx+r14*1-0x30]         | 4a 8d 5c 31 d0
lea rbx,[rdi*8+0x0]              | 48 8d 1c fd 00 00 00
lea rbx,[rdi+0x1]                | 48 8d 5f 01
lea rbx,[rdi+0x9]                | 48 8d 5f 09
lea rbx,[rdx+r15*1]              | 4a 8d 1c 3a
lea rbx,[rip+0x7885c]            | 48 8d 1d 5c 88 07 00
lea rbx,[rip+0x7983a]            | 48 8d 1d 3a 98 07 00
lea rbx,[rip+0x7993a]            | 48 8d 1d 3a 99 07 00
lea rbx,[rip+0x7a3d9]            | 48 8d 1d d9 a3 07 00
lea rbx,[rip+0x7a447]            | 48 8d 1d 47 a4 07 00
lea rbx,[rip+0x7a507]            | 48 8d 1d 07 a5 07 00
lea rbx,[rip+0x7a588]            | 48 8d 1d 88 a5 07 00
lea rbx,[rip+0x7a617]            | 48 8d 1d 17 a6 07 00
lea rbx,[rip+0x7a68d]            | 48 8d 1d 8d a6 07 00
lea rbx,[rip+0x7a862]            | 48 8d 1d 62 a8 07 00
lea rbx,[rip+0x7a8bf]            | 48 8d 1d bf a8 07 00
lea rbx,[rip+0x7a936]            | 48 8d 1d 36 a9 07 00
lea rbx,[rip+0x7a9ac]            | 48 8d 1d ac a9 07 00
lea rbx,[rip+0x7be74]            | 48 8d 1d 74 be 07 00
lea rbx,[rip+0x7c482]            | 48 8d 1d 82 c4 07 00
lea rbx,[rip+0x7c49e]            | 48 8d 1d 9e c4 07 00
lea rbx,[rip+0x7ca04]            | 48 8d 1d 04 ca 07 00
lea rbx,[rip+0x7ca5b]            | 48 8d 1d 5b ca 07 00
lea rbx,[rip+0x7caa5]            | 48 8d 1d a5 ca 07 00
lea rbx,[rip+0x7cafc]            | 48 8d 1d fc ca 07 00
lea rbx,[rip+0x7cc56]            | 48 8d 1d 56 cc 07 00
lea rbx,[rip+0x7cd48]            | 48 8d 1d 48 cd 07 00
lea rbx,[rip+0x7ce4d]            | 48 8d 1d 4d ce 07 00
lea rbx,[rip+0x7cf4a]            | 48 8d 1d 4a cf 07 00
lea rbx,[rip+0x7cfd8]            | 48 8d 1d d8 cf 07 00
lea rbx,[rip+0x7d058]            | 48 8d 1d 58 d0 07 00
lea rbx,[rip+0x7d0ff]            | 48 8d 1d ff d0 07 00
lea rbx,[rip+0x7d216]            | 48 8d 1d 16 d2 07 00
lea rbx,[rip+0x7d62b]            | 48 8d 1d 2b d6 07 00
lea rbx,[rip+0x7d645]            | 48 8d 1d 45 d6 07 00
lea rbx,[rip+0x7d6f1]            | 48 8d 1d f1 d6 07 00
lea rbx,[rip+0x7d865]            | 48 8d 1d 65 d8 07 00
lea rbx,[rip+0x7d9ae]            | 48 8d 1d ae d9 07 00
lea rbx,[rip+0x7da3f]            | 48 8d 1d 3f da 07 00
lea rbx,[rip+0x7dac3]            | 48 8d 1d c3 da 07 00
lea rbx,[rip+0x7db55]            | 48 8d 1d 55 db 07 00
lea rbx,[rip+0x7dbf0]            | 48 8d 1d f0 db 07 00
lea rbx,[rip+0x7dc80]            | 48 8d 1d 80 dc 07 00
lea rbx,[rip+0x7dd00]            | 48 8d 1d 00 dd 07 00
lea rbx,[rip+0x7dd14]            | 48 8d 1d 14 dd 07 00
lea rbx,[rip+0x7dda9]            | 48 8d 1d a9 dd 07 00
lea rbx,[rip+0x7dee4]            | 48 8d 1d e4 de 07 00
lea rbx,[rip+0x7df4e]            | 48 8d 1d 4e df 07 00
lea rbx,[rip+0x7df8e]            | 48 8d 1d 8e df 07 00
lea rbx,[rip+0x7dfd7]            | 48 8d 1d d7 df 07 00
lea rbx,[rip+0x7e075]            | 48 8d 1d 75 e0 07 00
lea rbx,[rip+0x7e100]            | 48 8d 1d 00 e1 07 00
lea rbx,[rip+0x7e5af]            | 48 8d 1d af e5 07 00
lea rbx,[rip+0x8ed49]            | 48 8d 1d 49 ed 08 00
lea rbx,[rip+0x9062d]            | 48 8d 1d 2d 06 09 00
lea rbx,[rsi+r14*1-0x30]         | 4a 8d 5c 36 d0
lea rbx,[rsi-0x1]                | 48 8d 5e ff
lea rbx,[rsp+0x100]              | 48 8d 9c 24 00 01 00
lea rbx,[rsp+0x10]               | 48 8d 5c 24 10
lea rbx,[rsp+0x20]               | 48 8d 5c 24 20
lea rbx,[rsp+0x4b0]              | 48 8d 9c 24 b0 04 00
lea rbx,[rsp+0x6f0]              | 48 8d 9c 24 f0 06 00
lea rbx,[rsp+0x88]               | 48 8d 9c 24 88 00 00
lea rbx,[rsp+0x8]                | 48 8d 5c 24 08
lea rbx,[rsp+0xb0]               | 48 8d 9c 24 b0 00 00
lea rbx,[rsp+0xf0]               | 48 8d 9c 24 f0 00 00
lea rbx,[rsp+0xf]                | 48 8d 5c 24 0f
lea rcx,[r10+0x1]                | 49 8d 4a 01
lea rcx,[r11+0x1]                | 49 8d 4b 01
lea rcx,[r11+0x48baa0]           | 49 8d 8b a0 ba 48 00
lea rcx,[r11+0x8]                | 49 8d 4b 08
lea rcx,[r11+r8*1]               | 4b 8d 0c 03
lea rcx,[r12+0x10]               | 49 8d 4c 24 10
lea rcx,[r12+0x1]                | 49 8d 4c 24 01
lea rcx,[r12+0x28]               | 49 8d 4c 24 28
lea rcx,[r12+0x40]               | 49 8d 4c 24 40
lea rcx,[r13+0x1]                | 49 8d 4d 01
lea rcx,[r13+r10*1+0x0]          | 4b 8d 4c 15 00
lea rcx,[r13+rax*1+0x0]          | 49 8d 4c 05 00
lea rcx,[r13+rdx*1+0x0]          | 49 8d 4c 15 00
lea rcx,[r14+0x1]                | 49 8d 4e 01
lea rcx,[r14+r12*1]              | 4b 8d 0c 26
lea rcx,[r14-0x18]               | 49 8d 4e e8
lea rcx,[r15+rax*1]              | 49 8d 0c 07
lea rcx,[r15+rcx*8]              | 49 8d 0c cf
lea rcx,[r8+0x1]                 | 49 8d 48 01
lea rcx,[r8+0x8]                 | 49 8d 48 08
lea rcx,[r8+rax*1-0x8]           | 49 8d 4c 00 f8
lea rcx,[r9+0x1]                 | 49 8d 49 01
lea rcx,[r9+0x30]                | 49 8d 49 30
lea rcx,[rax*8+0x0]              | 48 8d 0c c5 00 00 00
lea rcx,[rax+0x18]               | 48 8d 48 18
lea rcx,[rax+0x1]                | 48 8d 48 01
lea rcx,[rax+0x28]               | 48 8d 48 28
lea rcx,[rax+0x2]                | 48 8d 48 02
lea rcx,[rax+0x30]               | 48 8d 48 30
lea rcx,[rax+0x38]               | 48 8d 48 38
lea rcx,[rax+0x40]               | 48 8d 48 40
lea rcx,[rax+0x48]               | 48 8d 48 48
lea rcx,[rax+0x4]                | 48 8d 48 04
lea rcx,[rax+0x50]               | 48 8d 48 50
lea rcx,[rax+0x58]               | 48 8d 48 58
lea rcx,[rax+0x60]               | 48 8d 48 60
lea rcx,[rax+0x8]                | 48 8d 48 08
lea rcx,[rax+r12*1]              | 4a 8d 0c 20
lea rcx,[rax+r14*4]              | 4a 8d 0c b0
lea rcx,[rax+r8*1+0x1]           | 4a 8d 4c 00 01
lea rcx,[rax+rax*2]              | 48 8d 0c 40
lea rcx,[rax+rdx*1]              | 48 8d 0c 10
lea rcx,[rax+rsi*1]              | 48 8d 0c 30
lea rcx,[rax-0x8]                | 48 8d 48 f8
lea rcx,[rbp*8+0x0]              | 48 8d 0c ed 00 00 00
lea rcx,[rbp+0x10]               | 48 8d 4d 10
lea rcx,[rbp+0x3]                | 48 8d 4d 03
lea rcx,[rbp+0x4]                | 48 8d 4d 04
lea rcx,[rbp+0xb0c]              | 48 8d 8d 0c 0b 00 00
lea rcx,[rbp+0xb18]              | 48 8d 8d 18 0b 00 00
lea rcx,[rbp+0xb28]              | 48 8d 8d 28 0b 00 00
lea rcx,[rbp+0xb38]              | 48 8d 8d 38 0b 00 00
lea rcx,[rbp+r12*8+0x0]          | 4a 8d 4c e5 00
lea rcx,[rbp+rcx*1+0x8]          | 48 8d 4c 0d 08
lea rcx,[rbp+rdi*1+0x0]          | 48 8d 4c 3d 00
lea rcx,[rbp-0x1]                | 48 8d 4d ff
lea rcx,[rbp-0x38]               | 48 8d 4d c8
lea rcx,[rbp-0x3c]               | 48 8d 4d c4
lea rcx,[rbp-0xe0]               | 48 8d 8d 20 ff ff ff
lea rcx,[rbx+0x1]                | 48 8d 4b 01
lea rcx,[rbx+0x2]                | 48 8d 4b 02
lea rcx,[rbx+0x4]                | 48 8d 4b 04
lea rcx,[rbx+0x5]                | 48 8d 4b 05
lea rcx,[rbx+0x6]                | 48 8d 4b 06
lea rcx,[rbx+0x7]                | 48 8d 4b 07
lea rcx,[rbx+0x8]                | 48 8d 4b 08
lea rcx,[rbx+0xc]                | 48 8d 4b 0c
lea rcx,[rbx-0x50]               | 48 8d 4b b0
lea rcx,[rcx+rbp*8]              | 48 8d 0c e9
lea rcx,[rcx+rcx*2]              | 48 8d 0c 49
lea rcx,[rcx-0x1]                | 48 8d 49 ff
lea rcx,[rdi+0x10]               | 48 8d 4f 10
lea rcx,[rdi+0x1]                | 48 8d 4f 01
lea rcx,[rdi+0x48baa0]           | 48 8d 8f a0 ba 48 00
lea rcx,[rdx*8+0x0]              | 48 8d 0c d5 00 00 00
lea rcx,[rdx+0x1]                | 48 8d 4a 01
lea rcx,[rdx+0x2]                | 48 8d 4a 02
lea rcx,[rdx+rax*1-0x1]          | 48 8d 4c 02 ff
lea rcx,[rdx+rax*1]              | 48 8d 0c 02
lea rcx,[rdx+rdx*2]              | 48 8d 0c 52
lea rcx,[rip+0x754a3]            | 48 8d 0d a3 54 07 00
lea rcx,[rip+0x76ab3]            | 48 8d 0d b3 6a 07 00
lea rcx,[rip+0x77081]            | 48 8d 0d 81 70 07 00
lea rcx,[rip+0x7760a]            | 48 8d 0d 0a 76 07 00
lea rcx,[rip+0x776dd]            | 48 8d 0d dd 76 07 00
lea rcx,[rip+0x77ced]            | 48 8d 0d ed 7c 07 00
lea rcx,[rip+0x77dfc]            | 48 8d 0d fc 7d 07 00
lea rcx,[rip+0x7838f]            | 48 8d 0d 8f 83 07 00
lea rcx,[rip+0x78ac4]            | 48 8d 0d c4 8a 07 00
lea rcx,[rip+0x78c94]            | 48 8d 0d 94 8c 07 00
lea rcx,[rip+0x78da9]            | 48 8d 0d a9 8d 07 00
lea rcx,[rip+0x7cd03]            | 48 8d 0d 03 cd 07 00
lea rcx,[rip+0x7d710]            | 48 8d 0d 10 d7 07 00
lea rcx,[rip+0x818c3]            | 48 8d 0d c3 18 08 00
lea rcx,[rip+0x81957]            | 48 8d 0d 57 19 08 00
lea rcx,[rip+0x81b51]            | 48 8d 0d 51 1b 08 00
lea rcx,[rip+0x8c78f]            | 48 8d 0d 8f c7 08 00
lea rcx,[rsi+0x1]                | 48 8d 4e 01
lea rcx,[rsp+0x100]              | 48 8d 8c 24 00 01 00
lea rcx,[rsp+0x10]               | 48 8d 4c 24 10
lea rcx,[rsp+0x124]              | 48 8d 8c 24 24 01 00
lea rcx,[rsp+0x18]               | 48 8d 4c 24 18
lea rcx,[rsp+0x20]               | 48 8d 4c 24 20
lea rcx,[rsp+0x268]              | 48 8d 8c 24 68 02 00
lea rcx,[rsp+0x270]              | 48 8d 8c 24 70 02 00
lea rcx,[rsp+0x30]               | 48 8d 4c 24 30
lea rcx,[rsp+0x320]              | 48 8d 8c 24 20 03 00
lea rcx,[rsp+0x34]               | 48 8d 4c 24 34
lea rcx,[rsp+0x360]              | 48 8d 8c 24 60 03 00
lea rcx,[rsp+0x38]               | 48 8d 4c 24 38
lea rcx,[rsp+0x3a0]              | 48 8d 8c 24 a0 03 00
lea rcx,[rsp+0x3c]               | 48 8d 4c 24 3c
lea rcx,[rsp+0x3e0]              | 48 8d 8c 24 e0 03 00
lea rcx,[rsp+0x40]               | 48 8d 4c 24 40
lea rcx,[rsp+0x420]              | 48 8d 8c 24 20 04 00
lea rcx,[rsp+0x450]              | 48 8d 8c 24 50 04 00
lea rcx,[rsp+0x48]               | 48 8d 4c 24 48
lea rcx,[rsp+0x4]                | 48 8d 4c 24 04
lea rcx,[rsp+0x4b0]              | 48 8d 8c 24 b0 04 00
lea rcx,[rsp+0x58]               | 48 8d 4c 24 58
lea rcx,[rsp+0x8]                | 48 8d 4c 24 08
lea rcx,[rsp+0x90]               | 48 8d 8c 24 90 00 00
lea rcx,[rsp+0xc0]               | 48 8d 8c 24 c0 00 00
lea rcx,[rsp+0xd0]               | 48 8d 8c 24 d0 00 00
lea rcx,[rsp+0xf]                | 48 8d 4c 24 0f
lea rdi,[r10+0x1]                | 49 8d 7a 01
lea rdi,[r10+0x2]                | 49 8d 7a 02
lea rdi,[r10+0x5]                | 49 8d 7a 05
lea rdi,[r10+r13*1+0x1]          | 4b 8d 7c 2a 01
lea rdi,[r11+0x17]               | 49 8d 7b 17
lea rdi,[r11+0xcc]               | 49 8d bb cc 00 00 00
lea rdi,[r11+r12*1]              | 4b 8d 3c 23
lea rdi,[r11+rax*1]              | 49 8d 3c 03
lea rdi,[r11-0x2]                | 49 8d 7b fe
lea rdi,[r12+0x18]               | 49 8d 7c 24 18
lea rdi,[r12+0x1]                | 49 8d 7c 24 01
lea rdi,[r12+0x58]               | 49 8d 7c 24 58
lea rdi,[r12+0x68]               | 49 8d 7c 24 68
lea rdi,[r12+0xb0]               | 49 8d bc 24 b0 00 00
lea rdi,[r12+r10*1]              | 4b 8d 3c 14
lea rdi,[r12+r14*1+0x2]          | 4b 8d 7c 34 02
lea rdi,[r12+r14*1+0x8]          | 4b 8d 7c 34 08
lea rdi,[r12+r9*1]               | 4b 8d 3c 0c
lea rdi,[r12+rax*1+0x1]          | 49 8d 7c 04 01
lea rdi,[r12+rax*4]              | 49 8d 3c 84
lea rdi,[r12+rbp*1]              | 49 8d 3c 2c
lea rdi,[r13+0x1]                | 49 8d 7d 01
lea rdi,[r13+0x3]                | 49 8d 7d 03
lea rdi,[r13+0x8]                | 49 8d 7d 08
lea rdi,[r13+0xcc]               | 49 8d bd cc 00 00 00
lea rdi,[r13+r12*1+0x0]          | 4b 8d 7c 25 00
lea rdi,[r13+r15*8+0x0]          | 4b 8d 7c fd 00
lea rdi,[r13+rbx*8+0x0]          | 49 8d 7c dd 00
lea rdi,[r13+rdx*1+0x0]          | 49 8d 7c 15 00
lea rdi,[r14+0x1]                | 49 8d 7e 01
lea rdi,[r14+0x2]                | 49 8d 7e 02
lea rdi,[r14+0x3]                | 49 8d 7e 03
lea rdi,[r14+0x4]                | 49 8d 7e 04
lea rdi,[r14+0x5]                | 49 8d 7e 05
lea rdi,[r14+0x7]                | 49 8d 7e 07
lea rdi,[r14+r12*1]              | 4b 8d 3c 26
lea rdi,[r14+rax*1+0x32]         | 49 8d 7c 06 32
lea rdi,[r14+rax*1-0x6]          | 49 8d 7c 06 fa
lea rdi,[r14+rbx*1]              | 49 8d 3c 1e
lea rdi,[r14+rsi*1+0x9]          | 49 8d 7c 36 09
lea rdi,[r15*8+0x17]             | 4a 8d 3c fd 17 00 00
lea rdi,[r15+0x1]                | 49 8d 7f 01
lea rdi,[r15+0x3]                | 49 8d 7f 03
lea rdi,[r15+0x4]                | 49 8d 7f 04
lea rdi,[r15+0x5]                | 49 8d 7f 05
lea rdi,[r15+r10*1]              | 4b 8d 3c 17
lea rdi,[r15+r15*1]              | 4b 8d 3c 3f
lea rdi,[r15+rax*1]              | 49 8d 3c 07
lea rdi,[r15+rax*8]              | 49 8d 3c c7
lea rdi,[r15+rbp*1]              | 49 8d 3c 2f
lea rdi,[r15+rbx*8]              | 49 8d 3c df
lea rdi,[r15+rdi*8]              | 49 8d 3c ff
lea rdi,[r15+rsi*8]              | 49 8d 3c f7
lea rdi,[r15-0x2]                | 49 8d 7f fe
lea rdi,[r8+0x1]                 | 49 8d 78 01
lea rdi,[r8+0x8]                 | 49 8d 78 08
lea rdi,[r8+r13*1]               | 4b 8d 3c 28
lea rdi,[r8+rax*1+0x3]           | 49 8d 7c 00 03
lea rdi,[r8+rax*1]               | 49 8d 3c 00
lea rdi,[r8+rsi*8+0x8]           | 49 8d 7c f0 08
lea rdi,[r9+0x1]                 | 49 8d 79 01
lea rdi,[r9+0xc]                 | 49 8d 79 0c
lea rdi,[r9+0xcc]                | 49 8d b9 cc 00 00 00
lea rdi,[r9+r10*1]               | 4b 8d 3c 11
lea rdi,[r9+r13*1]               | 4b 8d 3c 29
lea rdi,[r9+r8*1+0x3]            | 4b 8d 7c 01 03
lea rdi,[r9+r8*1]                | 4b 8d 3c 01
lea rdi,[r9+rax*1]               | 49 8d 3c 01
lea rdi,[r9+rcx*8]               | 49 8d 3c c9
lea rdi,[r9+rsi*1]               | 49 8d 3c 31
lea rdi,[rax*8+0x0]              | 48 8d 3c c5 00 00 00
lea rdi,[rax+0x11]               | 48 8d 78 11
lea rdi,[rax+0x1]                | 48 8d 78 01
lea rdi,[rax+0x2]                | 48 8d 78 02
lea rdi,[rax+0x32]               | 48 8d 78 32
lea rdi,[rax+0x38]               | 48 8d 78 38
lea rdi,[rax+0x4]                | 48 8d 78 04
lea rdi,[rax+0x8]                | 48 8d 78 08
lea rdi,[rax+0xd0]               | 48 8d b8 d0 00 00 00
lea rdi,[rax+r12*1+0x1]          | 4a 8d 7c 20 01
lea rdi,[rax+r13*1+0x1]          | 4a 8d 7c 28 01
lea rdi,[rax+r13*1]              | 4a 8d 3c 28
lea rdi,[rax+r14*1+0x1]          | 4a 8d 7c 30 01
lea rdi,[rax+r15*1+0x8]          | 4a 8d 7c 38 08
lea rdi,[rax+r15*1]              | 4a 8d 3c 38
lea rdi,[rax+r8*1+0x20]          | 4a 8d 7c 00 20
lea rdi,[rax+rbp*1+0x3]          | 48 8d 7c 28 03
lea rdi,[rax+rbp*1]              | 48 8d 3c 28
lea rdi,[rax+rdx*1+0xe6c]        | 48 8d bc 10 6c 0e 00
lea rdi,[rax-0x18]               | 48 8d 78 e8
lea rdi,[rbp+0x108]              | 48 8d bd 08 01 00 00
lea rdi,[rbp+0x10]               | 48 8d 7d 10
lea rdi,[rbp+0x110]              | 48 8d bd 10 01 00 00
lea rdi,[rbp+0x18]               | 48 8d 7d 18
lea rdi,[rbp+0x1]                | 48 8d 7d 01
lea rdi,[rbp+0x58]               | 48 8d 7d 58
lea rdi,[rbp+0x6]                | 48 8d 7d 06
lea rdi,[rbp+0xb10]              | 48 8d bd 10 0b 00 00
lea rdi,[rbp+0xb20]              | 48 8d bd 20 0b 00 00
lea rdi,[rbp+0xb40]              | 48 8d bd 40 0b 00 00
lea rdi,[rbp+0xc]                | 48 8d 7d 0c
lea rdi,[rbp+r13*1+0x0]          | 4a 8d 7c 2d 00
lea rdi,[rbp+r13*1+0x18]         | 4a 8d 7c 2d 18
lea rdi,[rbp+r14*1+0x0]          | 4a 8d 7c 35 00
lea rdi,[rbp+r8*1+0x0]           | 4a 8d 7c 05 00
lea rdi,[rbp+rax*1+0x0]          | 48 8d 7c 05 00
lea rdi,[rbp-0x130]              | 48 8d bd d0 fe ff ff
lea rdi,[rbp-0x1b0]              | 48 8d bd 50 fe ff ff
lea rdi,[rbp-0x210]              | 48 8d bd f0 fd ff ff
lea rdi,[rbp-0x38]               | 48 8d 7d c8
lea rdi,[rbp-0x50]               | 48 8d 7d b0
lea rdi,[rbx+0x19]               | 48 8d 7b 19
lea rdi,[rbx+0x1]                | 48 8d 7b 01
lea rdi,[rbx+0x2]                | 48 8d 7b 02
lea rdi,[rbx+0x3]                | 48 8d 7b 03
lea rdi,[rbx+0x4]                | 48 8d 7b 04
lea rdi,[rbx+0x5]                | 48 8d 7b 05
lea rdi,[rbx+0x70]               | 48 8d 7b 70
lea rdi,[rbx+0x8]                | 48 8d 7b 08
lea rdi,[rbx+r12*1]              | 4a 8d 3c 23
lea rdi,[rbx+r13*1]              | 4a 8d 3c 2b
lea rdi,[rbx+r8*1+0x8]           | 4a 8d 7c 03 08
lea rdi,[rbx+rdx*1]              | 48 8d 3c 13
lea rdi,[rbx-0x10]               | 48 8d 7b f0
lea rdi,[rbx-0x18]               | 48 8d 7b e8
lea rdi,[rbx-0x8]                | 48 8d 7b f8
lea rdi,[rcx+0x1]                | 48 8d 79 01
lea rdi,[rcx+0xc]                | 48 8d 79 0c
lea rdi,[rcx+r12*1+0x10]         | 4a 8d 7c 21 10
lea rdi,[rcx+rax*1]              | 48 8d 3c 01
lea rdi,[rcx+rax*4]              | 48 8d 3c 81
lea rdi,[rcx+rdx*1]              | 48 8d 3c 11
lea rdi,[rcx+rsi*8-0x10]         | 48 8d 7c f1 f0
lea rdi,[rdi+0x19]               | 48 8d 7f 19
lea rdi,[rdi+0x1]                | 48 8d 7f 01
lea rdi,[rdi+0x2]                | 48 8d 7f 02
lea rdi,[rdi+0x3]                | 48 8d 7f 03
lea rdi,[rdi+0x40]               | 48 8d 7f 40
lea rdi,[rdi+0x8]                | 48 8d 7f 08
lea rdi,[rdi+rax*1+0x3]          | 48 8d 7c 07 03
lea rdi,[rdx+0x1]                | 48 8d 7a 01
lea rdi,[rdx+0x2]                | 48 8d 7a 02
lea rdi,[rdx+0x8]                | 48 8d 7a 08
lea rdi,[rdx+r10*1+0x8]          | 4a 8d 7c 12 08
lea rdi,[rdx+rax*1-0x1]          | 48 8d 7c 02 ff
lea rdi,[rdx+rax*1]              | 48 8d 3c 02
lea rdi,[rdx+rcx*1]              | 48 8d 3c 0a
lea rdi,[rdx+rsi*1]              | 48 8d 3c 32
lea rdi,[rdx-0x18]               | 48 8d 7a e8
lea rdi,[rip+0x800a9]            | 48 8d 3d a9 00 08 00
lea rdi,[rip+0x8178a]            | 48 8d 3d 8a 17 08 00
lea rdi,[rip+0x8184a]            | 48 8d 3d 4a 18 08 00
lea rdi,[rip+0x8190a]            | 48 8d 3d 0a 19 08 00
lea rdi,[rip+0x81f0a]            | 48 8d 3d 0a 1f 08 00
lea rdi,[rip+0x8204a]            | 48 8d 3d 4a 20 08 00
lea rdi,[rip+0x8219b]            | 48 8d 3d 9b 21 08 00
lea rdi,[rip+0x8220d]            | 48 8d 3d 0d 22 08 00
lea rdi,[rip+0x82218]            | 48 8d 3d 18 22 08 00
lea rdi,[rip+0x82254]            | 48 8d 3d 54 22 08 00
lea rdi,[rip+0x822e8]            | 48 8d 3d e8 22 08 00
lea rdi,[rip+0x82359]            | 48 8d 3d 59 23 08 00
lea rdi,[rip+0x824c4]            | 48 8d 3d c4 24 08 00
lea rdi,[rip+0x824f0]            | 48 8d 3d f0 24 08 00
lea rdi,[rip+0x8251f]            | 48 8d 3d 1f 25 08 00
lea rdi,[rip+0x82547]            | 48 8d 3d 47 25 08 00
lea rdi,[rip+0x82583]            | 48 8d 3d 83 25 08 00
lea rdi,[rip+0x825ff]            | 48 8d 3d ff 25 08 00
lea rdi,[rip+0x829dc]            | 48 8d 3d dc 29 08 00
lea rdi,[rip+0x82bfc]            | 48 8d 3d fc 2b 08 00
lea rdi,[rip+0x82f9e]            | 48 8d 3d 9e 2f 08 00
lea rdi,[rip+0x830d6]            | 48 8d 3d d6 30 08 00
lea rdi,[rip+0x83403]            | 48 8d 3d 03 34 08 00
lea rdi,[rip+0x8342f]            | 48 8d 3d 2f 34 08 00
lea rdi,[rip+0x83489]            | 48 8d 3d 89 34 08 00
lea rdi,[rip+0x834ea]            | 48 8d 3d ea 34 08 00
lea rdi,[rip+0x835a6]            | 48 8d 3d a6 35 08 00
lea rdi,[rip+0x836ce]            | 48 8d 3d ce 36 08 00
lea rdi,[rip+0x83825]            | 48 8d 3d 25 38 08 00
lea rdi,[rip+0x8393a]            | 48 8d 3d 3a 39 08 00
lea rdi,[rip+0x839cf]            | 48 8d 3d cf 39 08 00
lea rdi,[rip+0x83ab0]            | 48 8d 3d b0 3a 08 00
lea rdi,[rip+0x83b3c]            | 48 8d 3d 3c 3b 08 00
lea rdi,[rip+0x83bc4]            | 48 8d 3d c4 3b 08 00
lea rdi,[rip+0x84397]            | 48 8d 3d 97 43 08 00
lea rdi,[rip+0x84427]            | 48 8d 3d 27 44 08 00
lea rdi,[rip+0x84539]            | 48 8d 3d 39 45 08 00
lea rdi,[rip+0x84554]            | 48 8d 3d 54 45 08 00
lea rdi,[rip+0x845f6]            | 48 8d 3d f6 45 08 00
lea rdi,[rip+0x8484d]            | 48 8d 3d 4d 48 08 00
lea rdi,[rip+0x84d6f]            | 48 8d 3d 6f 4d 08 00
lea rdi,[rip+0x84e54]            | 48 8d 3d 54 4e 08 00
lea rdi,[rip+0x85199]            | 48 8d 3d 99 51 08 00
lea rdi,[rip+0x86817]            | 48 8d 3d 17 68 08 00
lea rdi,[rip+0x86a28]            | 48 8d 3d 28 6a 08 00
lea rdi,[rip+0x86ba8]            | 48 8d 3d a8 6b 08 00
lea rdi,[rip+0x8733c]            | 48 8d 3d 3c 73 08 00
lea rdi,[rip+0x873c6]            | 48 8d 3d c6 73 08 00
lea rdi,[rip+0x873c8]            | 48 8d 3d c8 73 08 00
lea rdi,[rip+0x87409]            | 48 8d 3d 09 74 08 00
lea rdi,[rip+0x8883c]            | 48 8d 3d 3c 88 08 00
lea rdi,[rip+0x88936]            | 48 8d 3d 36 89 08 00
lea rdi,[rip+0x88986]            | 48 8d 3d 86 89 08 00
lea rdi,[rip+0x889ce]            | 48 8d 3d ce 89 08 00
lea rdi,[rip+0x88bfa]            | 48 8d 3d fa 8b 08 00
lea rdi,[rip+0x88de7]            | 48 8d 3d e7 8d 08 00
lea rdi,[rip+0x88e74]            | 48 8d 3d 74 8e 08 00
lea rdi,[rip+0x88f89]            | 48 8d 3d 89 8f 08 00
lea rdi,[rip+0x88fa4]            | 48 8d 3d a4 8f 08 00
lea rdi,[rip+0x89049]            | 48 8d 3d 49 90 08 00
lea rdi,[rip+0x89877]            | 48 8d 3d 77 98 08 00
lea rdi,[rip+0x899ea]            | 48 8d 3d ea 99 08 00
lea rdi,[rip+0x89a1a]            | 48 8d 3d 1a 9a 08 00
lea rdi,[rip+0x89a1e]            | 48 8d 3d 1e 9a 08 00
lea rdi,[rip+0x89a46]            | 48 8d 3d 46 9a 08 00
lea rdi,[rip+0x89a96]            | 48 8d 3d 96 9a 08 00
lea rdi,[rip+0x89aea]            | 48 8d 3d ea 9a 08 00
lea rdi,[rip+0x89b2c]            | 48 8d 3d 2c 9b 08 00
lea rdi,[rip+0x89b47]            | 48 8d 3d 47 9b 08 00
lea rdi,[rip+0x89ba1]            | 48 8d 3d a1 9b 08 00
lea rdi,[rip+0x89bbe]            | 48 8d 3d be 9b 08 00
lea rdi,[rip+0x89c26]            | 48 8d 3d 26 9c 08 00
lea rdi,[rip+0x89c80]            | 48 8d 3d 80 9c 08 00
lea rdi,[rip+0x89cb5]            | 48 8d 3d b5 9c 08 00
lea rdi,[rip+0x89d05]            | 48 8d 3d 05 9d 08 00
lea rdi,[rip+0x89d61]            | 48 8d 3d 61 9d 08 00
lea rdi,[rip+0x89dcf]            | 48 8d 3d cf 9d 08 00
lea rdi,[rip+0x89e28]            | 48 8d 3d 28 9e 08 00
lea rdi,[rip+0x89e75]            | 48 8d 3d 75 9e 08 00
lea rdi,[rip+0x89ec5]            | 48 8d 3d c5 9e 08 00
lea rdi,[rip+0x89f18]            | 48 8d 3d 18 9f 08 00
lea rdi,[rip+0x89f63]            | 48 8d 3d 63 9f 08 00
lea rdi,[rip+0x89fdb]            | 48 8d 3d db 9f 08 00
lea rdi,[rip+0x8a041]            | 48 8d 3d 41 a0 08 00
lea rdi,[rip+0x8a08f]            | 48 8d 3d 8f a0 08 00
lea rdi,[rip+0x8a0d3]            | 48 8d 3d d3 a0 08 00
lea rdi,[rip+0x8a161]            | 48 8d 3d 61 a1 08 00
lea rdi,[rip+0x8a229]            | 48 8d 3d 29 a2 08 00
lea rdi,[rip+0x8a34a]            | 48 8d 3d 4a a3 08 00
lea rdi,[rip+0x8a62d]            | 48 8d 3d 2d a6 08 00
lea rdi,[rip+0x8a6c2]            | 48 8d 3d c2 a6 08 00
lea rdi,[rip+0x8a7be]            | 48 8d 3d be a7 08 00
lea rdi,[rip+0x8a90e]            | 48 8d 3d 0e a9 08 00
lea rdi,[rip+0x8ac24]            | 48 8d 3d 24 ac 08 00
lea rdi,[rip+0x8ac54]            | 48 8d 3d 54 ac 08 00
lea rdi,[rip+0x8b74f]            | 48 8d 3d 4f b7 08 00
lea rdi,[rip+0x8b859]            | 48 8d 3d 59 b8 08 00
lea rdi,[rip+0x8c3e5]            | 48 8d 3d e5 c3 08 00
lea rdi,[rip+0x8c421]            | 48 8d 3d 21 c4 08 00
lea rdi,[rip+0x8c57d]            | 48 8d 3d 7d c5 08 00
lea rdi,[rip+0x8c6a4]            | 48 8d 3d a4 c6 08 00
lea rdi,[rip+0x8c727]            | 48 8d 3d 27 c7 08 00
lea rdi,[rip+0x8c76f]            | 48 8d 3d 6f c7 08 00
lea rdi,[rip+0x8c7cf]            | 48 8d 3d cf c7 08 00
lea rdi,[rip+0x8c800]            | 48 8d 3d 00 c8 08 00
lea rdi,[rip+0x8c87c]            | 48 8d 3d 7c c8 08 00
lea rdi,[rip+0x8c8ad]            | 48 8d 3d ad c8 08 00
lea rdi,[rip+0x8c914]            | 48 8d 3d 14 c9 08 00
lea rdi,[rip+0x8c945]            | 48 8d 3d 45 c9 08 00
lea rdi,[rip+0x8c9e9]            | 48 8d 3d e9 c9 08 00
lea rdi,[rip+0x8ca1a]            | 48 8d 3d 1a ca 08 00
lea rdi,[rip+0x8caa3]            | 48 8d 3d a3 ca 08 00
lea rdi,[rip+0x8cad4]            | 48 8d 3d d4 ca 08 00
lea rdi,[rip+0x8f3ac]            | 48 8d 3d ac f3 08 00
lea rdi,[rip+0x9fc4a]            | 48 8d 3d 4a fc 09 00
lea rdi,[rip+0xffffffffffffee44] | 48 8d 3d 44 ee ff ff
lea rdi,[rsi+0x1]                | 48 8d 7e 01
lea rdi,[rsi+0x3]                | 48 8d 7e 03
lea rdi,[rsi+0x4]                | 48 8d 7e 04
lea rdi,[rsi+0x8]                | 48 8d 7e 08
lea rdi,[rsi+rax*1+0x1]          | 48 8d 7c 06 01
lea rdi,[rsi+rdi*8]              | 48 8d 3c fe
lea rdi,[rsp+0x100]              | 48 8d bc 24 00 01 00
lea rdi,[rsp+0x107]              | 48 8d bc 24 07 01 00
lea rdi,[rsp+0x108]              | 48 8d bc 24 08 01 00
lea rdi,[rsp+0x10]               | 48 8d 7c 24 10
lea rdi,[rsp+0x110]              | 48 8d bc 24 10 01 00
lea rdi,[rsp+0x120]              | 48 8d bc 24 20 01 00
lea rdi,[rsp+0x128]              | 48 8d bc 24 28 01 00
lea rdi,[rsp+0x140]              | 48 8d bc 24 40 01 00
lea rdi,[rsp+0x148]              | 48 8d bc 24 48 01 00
lea rdi,[rsp+0x168]              | 48 8d bc 24 68 01 00
lea rdi,[rsp+0x170]              | 48 8d bc 24 70 01 00
lea rdi,[rsp+0x188]              | 48 8d bc 24 88 01 00
lea rdi,[rsp+0x18]               | 48 8d 7c 24 18
lea rdi,[rsp+0x1a0]              | 48 8d bc 24 a0 01 00
lea rdi,[rsp+0x1a8]              | 48 8d bc 24 a8 01 00
lea rdi,[rsp+0x1b0]              | 48 8d bc 24 b0 01 00
lea rdi,[rsp+0x1b8]              | 48 8d bc 24 b8 01 00
lea rdi,[rsp+0x1c8]              | 48 8d bc 24 c8 01 00
lea rdi,[rsp+0x1e0]              | 48 8d bc 24 e0 01 00
lea rdi,[rsp+0x1e8]              | 48 8d bc 24 e8 01 00
lea rdi,[rsp+0x208]              | 48 8d bc 24 08 02 00
lea rdi,[rsp+0x20]               | 48 8d 7c 24 20
lea rdi,[rsp+0x220]              | 48 8d bc 24 20 02 00
lea rdi,[rsp+0x228]              | 48 8d bc 24 28 02 00
lea rdi,[rsp+0x230]              | 48 8d bc 24 30 02 00
lea rdi,[rsp+0x268]              | 48 8d bc 24 68 02 00
lea rdi,[rsp+0x288]              | 48 8d bc 24 88 02 00
lea rdi,[rsp+0x28]               | 48 8d 7c 24 28
lea rdi,[rsp+0x2e0]              | 48 8d bc 24 e0 02 00
lea rdi,[rsp+0x30]               | 48 8d 7c 24 30
lea rdi,[rsp+0x320]              | 48 8d bc 24 20 03 00
lea rdi,[rsp+0x360]              | 48 8d bc 24 60 03 00
lea rdi,[rsp+0x38]               | 48 8d 7c 24 38
lea rdi,[rsp+0x40]               | 48 8d 7c 24 40
lea rdi,[rsp+0x420]              | 48 8d bc 24 20 04 00
lea rdi,[rsp+0x48]               | 48 8d 7c 24 48
lea rdi,[rsp+0x4]                | 48 8d 7c 24 04
lea rdi,[rsp+0x4b0]              | 48 8d bc 24 b0 04 00
lea rdi,[rsp+0x4c]               | 48 8d 7c 24 4c
lea rdi,[rsp+0x50]               | 48 8d 7c 24 50
lea rdi,[rsp+0x5]                | 48 8d 7c 24 05
lea rdi,[rsp+0x5d0]              | 48 8d bc 24 d0 05 00
lea rdi,[rsp+0x60]               | 48 8d 7c 24 60
lea rdi,[rsp+0x68]               | 48 8d 7c 24 68
lea rdi,[rsp+0x6f0]              | 48 8d bc 24 f0 06 00
lea rdi,[rsp+0x70]               | 48 8d 7c 24 70
lea rdi,[rsp+0x78]               | 48 8d 7c 24 78
lea rdi,[rsp+0x7]                | 48 8d 7c 24 07
lea rdi,[rsp+0x80]               | 48 8d bc 24 80 00 00
lea rdi,[rsp+0x88]               | 48 8d bc 24 88 00 00
lea rdi,[rsp+0x8]                | 48 8d 7c 24 08
lea rdi,[rsp+0x90]               | 48 8d bc 24 90 00 00
lea rdi,[rsp+0x98]               | 48 8d bc 24 98 00 00
lea rdi,[rsp+0xa0]               | 48 8d bc 24 a0 00 00
lea rdi,[rsp+0xc0]               | 48 8d bc 24 c0 00 00
lea rdi,[rsp+0xc8]               | 48 8d bc 24 c8 00 00
lea rdi,[rsp+0xcc]               | 48 8d bc 24 cc 00 00
lea rdi,[rsp+0xd0]               | 48 8d bc 24 d0 00 00
lea rdi,[rsp+0xe0]               | 48 8d bc 24 e0 00 00
lea rdi,[rsp+0xf]                | 48 8d 7c 24 0f
lea rdi,[rsp+r11*8+0x50]         | 4a 8d 7c dc 50
lea rdx,[r10+0x1]                | 49 8d 52 01
lea rdx,[r10+rsi*1]              | 49 8d 14 32
lea rdx,[r11+0x1]                | 49 8d 53 01
lea rdx,[r11+0x78]               | 49 8d 53 78
lea rdx,[r11+r13*1]              | 4b 8d 14 2b
lea rdx,[r11+r15*1]              | 4b 8d 14 3b
lea rdx,[r11+r8*1]               | 4b 8d 14 03
lea rdx,[r11+r9*1]               | 4b 8d 14 0b
lea rdx,[r11+rdx*1+0x1]          | 49 8d 54 13 01
lea rdx,[r12+0x10]               | 49 8d 54 24 10
lea rdx,[r12+0x1]                | 49 8d 54 24 01
lea rdx,[r12+0x2]                | 49 8d 54 24 02
lea rdx,[r12+r11*1+0x1]          | 4b 8d 54 1c 01
lea rdx,[r12-0x1]                | 49 8d 54 24 ff
lea rdx,[r12-0x8]                | 49 8d 54 24 f8
lea rdx,[r13+0x1]                | 49 8d 55 01
lea rdx,[r13+0x2]                | 49 8d 55 02
lea rdx,[r13+0x58]               | 49 8d 55 58
lea rdx,[r13+rax*1+0x0]          | 49 8d 54 05 00
lea rdx,[r13+rcx*1+0x1]          | 49 8d 54 0d 01
lea rdx,[r13-0x1]                | 49 8d 55 ff
lea rdx,[r14+0x1]                | 49 8d 56 01
lea rdx,[r14+0x8]                | 49 8d 56 08
lea rdx,[r14-0x1]                | 49 8d 56 ff
lea rdx,[r15+0x18]               | 49 8d 57 18
lea rdx,[r15+r11*8]              | 4b 8d 14 df
lea rdx,[r15+r12*1]              | 4b 8d 14 27
lea rdx,[r15+rax*1]              | 49 8d 14 07
lea rdx,[r15-0x1]                | 49 8d 57 ff
lea rdx,[r8+0x1]                 | 49 8d 50 01
lea rdx,[r8+0x2]                 | 49 8d 50 02
lea rdx,[r8+0x30]                | 49 8d 50 30
lea rdx,[r8+0x3]                 | 49 8d 50 03
lea rdx,[r8+0x48]                | 49 8d 50 48
lea rdx,[r8+0x5]                 | 49 8d 50 05
lea rdx,[r8+0x60]                | 49 8d 50 60
lea rdx,[r8+0x78]                | 49 8d 50 78
lea rdx,[r8+0x90]                | 49 8d 90 90 00 00 00
lea rdx,[r8+0xa8]                | 49 8d 90 a8 00 00 00
lea rdx,[r8+rax*1+0x3]           | 49 8d 54 00 03
lea rdx,[r8-0x1]                 | 49 8d 50 ff
lea rdx,[r9+0x1]                 | 49 8d 51 01
lea rdx,[rax*8+0x0]              | 48 8d 14 c5 00 00 00
lea rdx,[rax+0x1]                | 48 8d 50 01
lea rdx,[rax+0x28]               | 48 8d 50 28
lea rdx,[rax+0x2]                | 48 8d 50 02
lea rdx,[rax+0x3]                | 48 8d 50 03
lea rdx,[rax+0x5]                | 48 8d 50 05
lea rdx,[rax+0x8]                | 48 8d 50 08
lea rdx,[rax+r8*1]               | 4a 8d 14 00
lea rdx,[rax+rbp*1]              | 48 8d 14 28
lea rdx,[rax+rcx*8]              | 48 8d 14 c8
lea rdx,[rax-0x18]               | 48 8d 50 e8
lea rdx,[rax-0x1]                | 48 8d 50 ff
lea rdx,[rbp*8+0x0]              | 48 8d 14 ed 00 00 00
lea rdx,[rbp+0x1]                | 48 8d 55 01
lea rdx,[rbp+0x28]               | 48 8d 55 28
lea rdx,[rbp+0x2]                | 48 8d 55 02
lea rdx,[rbp+0x8]                | 48 8d 55 08
lea rdx,[rbp+0xb4]               | 48 8d 95 b4 00 00 00
lea rdx,[rbp+r12*1+0x0]          | 4a 8d 54 25 00
lea rdx,[rbp+rax*1+0x0]          | 48 8d 54 05 00
lea rdx,[rbp+rbx*1+0x0]          | 48 8d 54 1d 00
lea rdx,[rbp+rcx*1+0xa6a]        | 48 8d 94 0d 6a 0a 00
lea rdx,[rbp+rdi*1+0xc6c]        | 48 8d 94 3d 6c 0c 00
lea rdx,[rbp-0x1]                | 48 8d 55 ff
lea rdx,[rbp-0x228]              | 48 8d 95 d8 fd ff ff
lea rdx,[rbp-0x38]               | 48 8d 55 c8
lea rdx,[rbx*8+0x0]              | 48 8d 14 dd 00 00 00
lea rdx,[rbx+0x1]                | 48 8d 53 01
lea rdx,[rbx+0x28]               | 48 8d 53 28
lea rdx,[rbx+0x78]               | 48 8d 53 78
lea rdx,[rbx+0x8]                | 48 8d 53 08
lea rdx,[rbx+rbx*1]              | 48 8d 14 1b
lea rdx,[rbx+rbx*2]              | 48 8d 14 5b
lea rdx,[rbx+rsi*1]              | 48 8d 14 33
lea rdx,[rbx-0x8]                | 48 8d 53 f8
lea rdx,[rcx+0x10]               | 48 8d 51 10
lea rdx,[rcx+0x1]                | 48 8d 51 01
lea rdx,[rcx+0x8]                | 48 8d 51 08
lea rdx,[rcx+r8*1+0x644]         | 4a 8d 94 01 44 06 00
lea rdx,[rdi+0x10]               | 48 8d 57 10
lea rdx,[rdi+0x1]                | 48 8d 57 01
lea rdx,[rdi+0x28]               | 48 8d 57 28
lea rdx,[rdi+0x2]                | 48 8d 57 02
lea rdx,[rdi+0x3]                | 48 8d 57 03
lea rdx,[rdi+0x70]               | 48 8d 57 70
lea rdx,[rdx+rax*1+0xa68]        | 48 8d 94 02 68 0a 00
lea rdx,[rip+0x13a89]            | 48 8d 15 89 3a 01 00
lea rdx,[rip+0x75a4d]            | 48 8d 15 4d 5a 07 00
lea rdx,[rip+0x78453]            | 48 8d 15 53 84 07 00
lea rdx,[rip+0x785f1]            | 48 8d 15 f1 85 07 00
lea rdx,[rip+0x7a655]            | 48 8d 15 55 a6 07 00
lea rdx,[rip+0x7affa]            | 48 8d 15 fa af 07 00
lea rdx,[rip+0x7e3ad]            | 48 8d 15 ad e3 07 00
lea rdx,[rip+0x7ed31]            | 48 8d 15 31 ed 07 00
lea rdx,[rip+0x80d90]            | 48 8d 15 90 0d 08 00
lea rdx,[rip+0x8106a]            | 48 8d 15 6a 10 08 00
lea rdx,[rip+0x88091]            | 48 8d 15 91 80 08 00
lea rdx,[rip+0x8d46c]            | 48 8d 15 6c d4 08 00
lea rdx,[rip+0x8d4d5]            | 48 8d 15 d5 d4 08 00
lea rdx,[rip+0x8efd2]            | 48 8d 15 d2 ef 08 00
lea rdx,[rip+0x94f63]            | 48 8d 15 63 4f 09 00
lea rdx,[rip+0x9554b]            | 48 8d 15 4b 55 09 00
lea rdx,[rip+0xfffffffffffffd7a] | 48 8d 15 7a fd ff ff
lea rdx,[rsi*4+0x0]              | 48 8d 14 b5 00 00 00
lea rdx,[rsi*8+0x0]              | 48 8d 14 f5 00 00 00
lea rdx,[rsi+0x1]                | 48 8d 56 01
lea rdx,[rsi+0x2]                | 48 8d 56 02
lea rdx,[rsi+0x38]               | 48 8d 56 38
lea rdx,[rsi+0x8]                | 48 8d 56 08
lea rdx,[rsi+r8*1]               | 4a 8d 14 06
lea rdx,[rsi+rsi*1]              | 48 8d 14 36
lea rdx,[rsi-0x1]                | 48 8d 56 ff
lea rdx,[rsp+0x10]               | 48 8d 54 24 10
lea rdx,[rsp+0x120]              | 48 8d 94 24 20 01 00
lea rdx,[rsp+0x128]              | 48 8d 94 24 28 01 00
lea rdx,[rsp+0x168]              | 48 8d 94 24 68 01 00
lea rdx,[rsp+0x18]               | 48 8d 54 24 18
lea rdx,[rsp+0x190]              | 48 8d 94 24 90 01 00
lea rdx,[rsp+0x20]               | 48 8d 54 24 20
lea rdx,[rsp+0x28]               | 48 8d 54 24 28
lea rdx,[rsp+0x2a8]              | 48 8d 94 24 a8 02 00
lea rdx,[rsp+0x320]              | 48 8d 94 24 20 03 00
lea rdx,[rsp+0x390]              | 48 8d 94 24 90 03 00
lea rdx,[rsp+0x40]               | 48 8d 54 24 40
lea rdx,[rsp+0x48]               | 48 8d 54 24 48
lea rdx,[rsp+0x4]                | 48 8d 54 24 04
lea rdx,[rsp+0x50]               | 48 8d 54 24 50
lea rdx,[rsp+0x60]               | 48 8d 54 24 60
lea rdx,[rsp+0x70]               | 48 8d 54 24 70
lea rdx,[rsp+0x78]               | 48 8d 54 24 78
lea rdx,[rsp+0x7]                | 48 8d 54 24 07
lea rdx,[rsp+0x80]               | 48 8d 94 24 80 00 00
lea rdx,[rsp+0x810]              | 48 8d 94 24 10 08 00
lea rdx,[rsp+0x88]               | 48 8d 94 24 88 00 00
lea rdx,[rsp+0x8]                | 48 8d 54 24 08
lea rdx,[rsp+0x9]                | 48 8d 54 24 09
lea rdx,[rsp+0xc0]               | 48 8d 94 24 c0 00 00
lea rdx,[rsp+0xc]                | 48 8d 54 24 0c
lea rdx,[rsp+0xcc]               | 48 8d 94 24 cc 00 00
lea rdx,[rsp+0xd0]               | 48 8d 94 24 d0 00 00
lea rdx,[rsp+0xe0]               | 48 8d 94 24 e0 00 00
lea rdx,[rsp+0xf]                | 48 8d 54 24 0f
lea rsi,[r10*4+0x8]              | 4a 8d 34 95 08 00 00
lea rsi,[r10*8+0x0]              | 4a 8d 34 d5 00 00 00
lea rsi,[r10+0x1]                | 49 8d 72 01
lea rsi,[r11*8+0x8]              | 4a 8d 34 dd 08 00 00
lea rsi,[r11+0x18]               | 49 8d 73 18
lea rsi,[r11+0x1]                | 49 8d 73 01
lea rsi,[r11+0xcc]               | 49 8d b3 cc 00 00 00
lea rsi,[r12+0x1]                | 49 8d 74 24 01
lea rsi,[r12+0xa8]               | 49 8d b4 24 a8 00 00
lea rsi,[r12+rax*1+0x18]         | 49 8d 74 04 18
lea rsi,[r12+rax*1+0x2]          | 49 8d 74 04 02
lea rsi,[r12+rax*1-0x2]          | 49 8d 74 04 fe
lea rsi,[r12+rbx*1]              | 49 8d 34 1c
lea rsi,[r13+0x1000]             | 49 8d b5 00 10 00 00
lea rsi,[r13+0x18]               | 49 8d 75 18
lea rsi,[r13+0x1]                | 49 8d 75 01
lea rsi,[r13+0x8]                | 49 8d 75 08
lea rsi,[r13+r10*8+0x0]          | 4b 8d 74 d5 00
lea rsi,[r13+r15*1+0x0]          | 4b 8d 74 3d 00
lea rsi,[r13+r9*1+0x0]           | 4b 8d 74 0d 00
lea rsi,[r13+rax*1+0x2]          | 49 8d 74 05 02
lea rsi,[r13+rax*1+0x4]          | 49 8d 74 05 04
lea rsi,[r13+rbx*8+0x8]          | 49 8d 74 dd 08
lea rsi,[r13+rcx*8+0x8]          | 49 8d 74 cd 08
lea rsi,[r14+r15*1]              | 4b 8d 34 3e
lea rsi,[r14+rax*1+0x2]          | 49 8d 74 06 02
lea rsi,[r14+rbx*1-0x3]          | 49 8d 74 1e fd
lea rsi,[r14-0x1]                | 49 8d 76 ff
lea rsi,[r15+r10*8+0x80]         | 4b 8d b4 d7 80 00 00
lea rsi,[r15+r12*1]              | 4b 8d 34 27
lea rsi,[r15+rbp*1+0x8]          | 49 8d 74 2f 08
lea rsi,[r15+rsi*8]              | 49 8d 34 f7
lea rsi,[r15-0x1]                | 49 8d 77 ff
lea rsi,[r8*8+0x8]               | 4a 8d 34 c5 08 00 00
lea rsi,[r8+0x1]                 | 49 8d 70 01
lea rsi,[r8+0x8]                 | 49 8d 70 08
lea rsi,[r8+0xcc]                | 49 8d b0 cc 00 00 00
lea rsi,[r8+r10*1-0x1]           | 4b 8d 74 10 ff
lea rsi,[r8+rcx*1]               | 49 8d 34 08
lea rsi,[r8-0x1]                 | 49 8d 70 ff
lea rsi,[r9*8+0x0]               | 4a 8d 34 cd 00 00 00
lea rsi,[r9+0x1]                 | 49 8d 71 01
lea rsi,[r9+0xf]                 | 49 8d 71 0f
lea rsi,[r9+r13*1]               | 4b 8d 34 29
lea rsi,[r9+r8*1]                | 4b 8d 34 01
lea rsi,[r9+rax*1+0x4]           | 49 8d 74 01 04
lea rsi,[rax*4+0x0]              | 48 8d 34 85 00 00 00
lea rsi,[rax*8+0x0]              | 48 8d 34 c5 00 00 00
lea rsi,[rax*8+0x8]              | 48 8d 34 c5 08 00 00
lea rsi,[rax+0x10]               | 48 8d 70 10
lea rsi,[rax+0x17]               | 48 8d 70 17
lea rsi,[rax+0x1]                | 48 8d 70 01
lea rsi,[rax+0x200]              | 48 8d b0 00 02 00 00
lea rsi,[rax+0x2]                | 48 8d 70 02
lea rsi,[rax+0x48baa0]           | 48 8d b0 a0 ba 48 00
lea rsi,[rax+0x4]                | 48 8d 70 04
lea rsi,[rax+0x68]               | 48 8d 70 68
lea rsi,[rax+0x70]               | 48 8d 70 70
lea rsi,[rax+0x78]               | 48 8d 70 78
lea rsi,[rax+0x80]               | 48 8d b0 80 00 00 00
lea rsi,[rax+0x88]               | 48 8d b0 88 00 00 00
lea rsi,[rax+0x8]                | 48 8d 70 08
lea rsi,[rax+0x90]               | 48 8d b0 90 00 00 00
lea rsi,[rax+0x98]               | 48 8d b0 98 00 00 00
lea rsi,[rax+r10*8-0x10]         | 4a 8d 74 d0 f0
lea rsi,[rax+r9*1+0x2]           | 4a 8d 74 08 02
lea rsi,[rax+rbx*1]              | 48 8d 34 18
lea rsi,[rax+rcx*1]              | 48 8d 34 08
lea rsi,[rax+rdx*1]              | 48 8d 34 10
lea rsi,[rax-0x1]                | 48 8d 70 ff
lea rsi,[rbp*8+0x8]              | 48 8d 34 ed 08 00 00
lea rsi,[rbp+0x10]               | 48 8d 75 10
lea rsi,[rbp+0x2]                | 48 8d 75 02
lea rsi,[rbp+0x40]               | 48 8d 75 40
lea rsi,[rbp+0x8]                | 48 8d 75 08
lea rsi,[rbp+r13*1+0x0]          | 4a 8d 74 2d 00
lea rsi,[rbp+r13*1-0x1]          | 4a 8d 74 2d ff
lea rsi,[rbp+r8*1+0x0]           | 4a 8d 74 05 00
lea rsi,[rbp+r9*1+0x0]           | 4a 8d 74 0d 00
lea rsi,[rbp+rax*1+0x0]          | 48 8d 74 05 00
lea rsi,[rbp+rbx*1+0x0]          | 48 8d 74 1d 00
lea rsi,[rbp-0x38]               | 48 8d 75 c8
lea rsi,[rbp-0x4f]               | 48 8d 75 b1
lea rsi,[rbp-0x50]               | 48 8d 75 b0
lea rsi,[rbp-0x80]               | 48 8d 75 80
lea rsi,[rbp-0xb0]               | 48 8d b5 50 ff ff ff
lea rsi,[rbp-0xc0]               | 48 8d b5 40 ff ff ff
lea rsi,[rbp-0xc4]               | 48 8d b5 3c ff ff ff
lea rsi,[rbp-0xc8]               | 48 8d b5 38 ff ff ff
lea rsi,[rbx+0x18]               | 48 8d 73 18
lea rsi,[rbx+0x1]                | 48 8d 73 01
lea rsi,[rbx+0x48baa0]           | 48 8d b3 a0 ba 48 00
lea rsi,[rbx+0x8]                | 48 8d 73 08
lea rsi,[rbx+r10*1+0x8]          | 4a 8d 74 13 08
lea rsi,[rbx+r15*1+0x8]          | 4a 8d 74 3b 08
lea rsi,[rbx+rax*1+0x2]          | 48 8d 74 03 02
lea rsi,[rbx+rax*1+0x4]          | 48 8d 74 03 04
lea rsi,[rbx+rdx*1]              | 48 8d 34 13
lea rsi,[rbx-0x1]                | 48 8d 73 ff
lea rsi,[rbx-0x4]                | 48 8d 73 fc
lea rsi,[rbx-0xc]                | 48 8d 73 f4
lea rsi,[rcx*8+0x8]              | 48 8d 34 cd 08 00 00
lea rsi,[rcx+0x1]                | 48 8d 71 01
lea rsi,[rcx+0x4]                | 48 8d 71 04
lea rsi,[rcx+0x70]               | 48 8d 71 70
lea rsi,[rcx+0x8]                | 48 8d 71 08
lea rsi,[rcx+r8*1]               | 4a 8d 34 01
lea rsi,[rcx+rax*1+0x1]          | 48 8d 74 01 01
lea rsi,[rcx+rax*1+0x3]          | 48 8d 74 01 03
lea rsi,[rcx+rbx*1+0x28]         | 48 8d 74 19 28
lea rsi,[rcx+rbx*1-0x1]          | 48 8d 74 19 ff
lea rsi,[rcx+rdx*1+0x1]          | 48 8d 74 11 01
lea rsi,[rcx-0x1]                | 48 8d 71 ff
lea rsi,[rdi*8+0x8]              | 48 8d 34 fd 08 00 00
lea rsi,[rdi+0x1]                | 48 8d 77 01
lea rsi,[rdi+0x20]               | 48 8d 77 20
lea rsi,[rdi+0x2]                | 48 8d 77 02
lea rsi,[rdi+0x3]                | 48 8d 77 03
lea rsi,[rdi+0x5]                | 48 8d 77 05
lea rsi,[rdi+0x9]                | 48 8d 77 09
lea rsi,[rdi+0xcc]               | 48 8d b7 cc 00 00 00
lea rsi,[rdi+r10*8]              | 4a 8d 34 d7
lea rsi,[rdi+r11*1]              | 4a 8d 34 1f
lea rsi,[rdi+r8*1]               | 4a 8d 34 07
lea rsi,[rdi+r8*8]               | 4a 8d 34 c7
lea rsi,[rdi+r9*1]               | 4a 8d 34 0f
lea rsi,[rdi+rax*1]              | 48 8d 34 07
lea rsi,[rdi+rcx*1]              | 48 8d 34 0f
lea rsi,[rdi+rdx*1]              | 48 8d 34 17
lea rsi,[rdi-0x825]              | 48 8d b7 db f7 ff ff
lea rsi,[rdx*8+0x0]              | 48 8d 34 d5 00 00 00
lea rsi,[rdx*8+0x17]             | 48 8d 34 d5 17 00 00
lea rsi,[rdx*8+0x8]              | 48 8d 34 d5 08 00 00
lea rsi,[rdx+0x180]              | 48 8d b2 80 01 00 00
lea rsi,[rdx+0x188]              | 48 8d b2 88 01 00 00
lea rsi,[rdx+0x1]                | 48 8d 72 01
lea rsi,[rdx+0x3]                | 48 8d 72 03
lea rsi,[rdx+0x6]                | 48 8d 72 06
lea rsi,[rdx+0x7]                | 48 8d 72 07
lea rsi,[rdx+0x8]                | 48 8d 72 08
lea rsi,[rdx+rbx*1]              | 48 8d 34 1a
lea rsi,[rdx+rcx*8-0x10]         | 48 8d 74 ca f0
lea rsi,[rdx+rdx*4]              | 48 8d 34 92
lea rsi,[rdx-0x7]                | 48 8d 72 f9
lea rsi,[rip+0x13c7e]            | 48 8d 35 7e 3c 01 00
lea rsi,[rip+0x18706]            | 48 8d 35 06 87 01 00
lea rsi,[rip+0x1878e]            | 48 8d 35 8e 87 01 00
lea rsi,[rip+0x6c7be]            | 48 8d 35 be c7 06 00
lea rsi,[rip+0x7961f]            | 48 8d 35 1f 96 07 00
lea rsi,[rip+0x7aaa0]            | 48 8d 35 a0 aa 07 00
lea rsi,[rip+0x7ac40]            | 48 8d 35 40 ac 07 00
lea rsi,[rip+0x7affb]            | 48 8d 35 fb af 07 00
lea rsi,[rip+0x7b193]            | 48 8d 35 93 b1 07 00
lea rsi,[rip+0x7b417]            | 48 8d 35 17 b4 07 00
lea rsi,[rip+0x7b4bf]            | 48 8d 35 bf b4 07 00
lea rsi,[rip+0x7b765]            | 48 8d 35 65 b7 07 00
lea rsi,[rip+0x7bcb7]            | 48 8d 35 b7 bc 07 00
lea rsi,[rip+0x7bcbb]            | 48 8d 35 bb bc 07 00
lea rsi,[rip+0x7bd30]            | 48 8d 35 30 bd 07 00
lea rsi,[rip+0x7d135]            | 48 8d 35 35 d1 07 00
lea rsi,[rip+0x7d18d]            | 48 8d 35 8d d1 07 00
lea rsi,[rip+0x7d5b3]            | 48 8d 35 b3 d5 07 00
lea rsi,[rip+0x7d643]            | 48 8d 35 43 d6 07 00
lea rsi,[rip+0x7d723]            | 48 8d 35 23 d7 07 00
lea rsi,[rip+0x7d7b3]            | 48 8d 35 b3 d7 07 00
lea rsi,[rip+0x7d893]            | 48 8d 35 93 d8 07 00
lea rsi,[rip+0x7d923]            | 48 8d 35 23 d9 07 00
lea rsi,[rip+0x7da23]            | 48 8d 35 23 da 07 00
lea rsi,[rip+0x7dab3]            | 48 8d 35 b3 da 07 00
lea rsi,[rip+0x7db93]            | 48 8d 35 93 db 07 00
lea rsi,[rip+0x7dc23]            | 48 8d 35 23 dc 07 00
lea rsi,[rip+0x7dd03]            | 48 8d 35 03 dd 07 00
lea rsi,[rip+0x7dd93]            | 48 8d 35 93 dd 07 00
lea rsi,[rip+0x7de73]            | 48 8d 35 73 de 07 00
lea rsi,[rip+0x7de7c]            | 48 8d 35 7c de 07 00
lea rsi,[rip+0x7df03]            | 48 8d 35 03 df 07 00
lea rsi,[rip+0x7dfe3]            | 48 8d 35 e3 df 07 00
lea rsi,[rip+0x7e073]            | 48 8d 35 73 e0 07 00
lea rsi,[rip+0x7e173]            | 48 8d 35 73 e1 07 00
lea rsi,[rip+0x7e203]            | 48 8d 35 03 e2 07 00
lea rsi,[rip+0x7e313]            | 48 8d 35 13 e3 07 00
lea rsi,[rip+0x7e3dd]            | 48 8d 35 dd e3 07 00
lea rsi,[rip+0x801ea]            | 48 8d 35 ea 01 08 00
lea rsi,[rip+0x82219]            | 48 8d 35 19 22 08 00
lea rsi,[rip+0x82220]            | 48 8d 35 20 22 08 00
lea rsi,[rip+0x82264]            | 48 8d 35 64 22 08 00
lea rsi,[rip+0x82279]            | 48 8d 35 79 22 08 00
lea rsi,[rip+0x822a2]            | 48 8d 35 a2 22 08 00
lea rsi,[rip+0x822d6]            | 48 8d 35 d6 22 08 00
lea rsi,[rip+0x82669]            | 48 8d 35 69 26 08 00
lea rsi,[rip+0x826ea]            | 48 8d 35 ea 26 08 00
lea rsi,[rip+0x826fc]            | 48 8d 35 fc 26 08 00
lea rsi,[rip+0x82778]            | 48 8d 35 78 27 08 00
lea rsi,[rip+0x831e3]            | 48 8d 35 e3 31 08 00
lea rsi,[rip+0x83486]            | 48 8d 35 86 34 08 00
lea rsi,[rip+0x8353f]            | 48 8d 35 3f 35 08 00
lea rsi,[rip+0x8358e]            | 48 8d 35 8e 35 08 00
lea rsi,[rip+0x837c6]            | 48 8d 35 c6 37 08 00
lea rsi,[rip+0x83a03]            | 48 8d 35 03 3a 08 00
lea rsi,[rip+0x83a73]            | 48 8d 35 73 3a 08 00
lea rsi,[rip+0x83baf]            | 48 8d 35 af 3b 08 00
lea rsi,[rip+0x844ce]            | 48 8d 35 ce 44 08 00
lea rsi,[rip+0x8455e]            | 48 8d 35 5e 45 08 00
lea rsi,[rip+0x84670]            | 48 8d 35 70 46 08 00
lea rsi,[rip+0x8468b]            | 48 8d 35 8b 46 08 00
lea rsi,[rip+0x8472d]            | 48 8d 35 2d 47 08 00
lea rsi,[rip+0x84e93]            | 48 8d 35 93 4e 08 00
lea rsi,[rip+0x889df]            | 48 8d 35 df 89 08 00
lea rsi,[rip+0x88acc]            | 48 8d 35 cc 8a 08 00
lea rsi,[rip+0x88b01]            | 48 8d 35 01 8b 08 00
lea rsi,[rip+0x88b49]            | 48 8d 35 49 8b 08 00
lea rsi,[rip+0x88f1e]            | 48 8d 35 1e 8f 08 00
lea rsi,[rip+0x88fab]            | 48 8d 35 ab 8f 08 00
lea rsi,[rip+0x890c0]            | 48 8d 35 c0 90 08 00
lea rsi,[rip+0x890db]            | 48 8d 35 db 90 08 00
lea rsi,[rip+0x89180]            | 48 8d 35 80 91 08 00
lea rsi,[rip+0x8999b]            | 48 8d 35 9b 99 08 00
lea rsi,[rip+0x89b2b]            | 48 8d 35 2b 9b 08 00
lea rsi,[rip+0x89bf7]            | 48 8d 35 f7 9b 08 00
lea rsi,[rip+0x89bfd]            | 48 8d 35 fd 9b 08 00
lea rsi,[rip+0x89c6c]            | 48 8d 35 6c 9c 08 00
lea rsi,[rip+0x89c87]            | 48 8d 35 87 9c 08 00
lea rsi,[rip+0x89cdf]            | 48 8d 35 df 9c 08 00
lea rsi,[rip+0x89d39]            | 48 8d 35 39 9d 08 00
lea rsi,[rip+0x89d7e]            | 48 8d 35 7e 9d 08 00
lea rsi,[rip+0x89dce]            | 48 8d 35 ce 9d 08 00
lea rsi,[rip+0x89e1a]            | 48 8d 35 1a 9e 08 00
lea rsi,[rip+0x89e9a]            | 48 8d 35 9a 9e 08 00
lea rsi,[rip+0x89ef1]            | 48 8d 35 f1 9e 08 00
lea rsi,[rip+0x89f3e]            | 48 8d 35 3e 9f 08 00
lea rsi,[rip+0x89f8e]            | 48 8d 35 8e 9f 08 00
lea rsi,[rip+0x89fe1]            | 48 8d 35 e1 9f 08 00
lea rsi,[rip+0x8a02c]            | 48 8d 35 2c a0 08 00
lea rsi,[rip+0x8a0a4]            | 48 8d 35 a4 a0 08 00
lea rsi,[rip+0x8a0fa]            | 48 8d 35 fa a0 08 00
lea rsi,[rip+0x8a148]            | 48 8d 35 48 a1 08 00
lea rsi,[rip+0x8a18c]            | 48 8d 35 8c a1 08 00
lea rsi,[rip+0x8a22a]            | 48 8d 35 2a a2 08 00
lea rsi,[rip+0x8a323]            | 48 8d 35 23 a3 08 00
lea rsi,[rip+0x8a6f6]            | 48 8d 35 f6 a6 08 00
lea rsi,[rip+0x8a766]            | 48 8d 35 66 a7 08 00
lea rsi,[rip+0x8a981]            | 48 8d 35 81 a9 08 00
lea rsi,[rip+0x8e439]            | 48 8d 35 39 e4 08 00
lea rsi,[rip+0x8f0a2]            | 48 8d 35 a2 f0 08 00
lea rsi,[rip+0x9584]             | 48 8d 35 84 95 00 00
lea rsi,[rip+0xa21df]            | 48 8d 35 df 21 0a 00
lea rsi,[rip+0xc9d9]             | 48 8d 35 d9 c9 00 00
lea rsi,[rip+0xe490]             | 48 8d 35 90 e4 00 00
lea rsi,[rip+0xffffffffffff64c6] | 48 8d 35 c6 64 ff ff
lea rsi,[rip+0xffffffffffffe083] | 48 8d 35 83 e0 ff ff
lea rsi,[rsi*8+0x8]              | 48 8d 34 f5 08 00 00
lea rsi,[rsi+rdi*1]              | 48 8d 34 3e
lea rsi,[rsi+rdx*1]              | 48 8d 34 16
lea rsi,[rsi-0x1]                | 48 8d 76 ff
lea rsi,[rsp+0x100]              | 48 8d b4 24 00 01 00
lea rsi,[rsp+0x108]              | 48 8d b4 24 08 01 00
lea rsi,[rsp+0x10]               | 48 8d 74 24 10
lea rsi,[rsp+0x110]              | 48 8d b4 24 10 01 00
lea rsi,[rsp+0x150]              | 48 8d b4 24 50 01 00
lea rsi,[rsp+0x170]              | 48 8d b4 24 70 01 00
lea rsi,[rsp+0x18]               | 48 8d 74 24 18
lea rsi,[rsp+0x20]               | 48 8d 74 24 20
lea rsi,[rsp+0x228]              | 48 8d b4 24 28 02 00
lea rsi,[rsp+0x268]              | 48 8d b4 24 68 02 00
lea rsi,[rsp+0x288]              | 48 8d b4 24 88 02 00
lea rsi,[rsp+0x28]               | 48 8d 74 24 28
lea rsi,[rsp+0x290]              | 48 8d b4 24 90 02 00
lea rsi,[rsp+0x2c]               | 48 8d 74 24 2c
lea rsi,[rsp+0x30]               | 48 8d 74 24 30
lea rsi,[rsp+0x320]              | 48 8d b4 24 20 03 00
lea rsi,[rsp+0x38]               | 48 8d 74 24 38
lea rsi,[rsp+0x3a0]              | 48 8d b4 24 a0 03 00
lea rsi,[rsp+0x40]               | 48 8d 74 24 40
lea rsi,[rsp+0x420]              | 48 8d b4 24 20 04 00
lea rsi,[rsp+0x48]               | 48 8d 74 24 48
lea rsi,[rsp+0x4]                | 48 8d 74 24 04
lea rsi,[rsp+0x4b0]              | 48 8d b4 24 b0 04 00
lea rsi,[rsp+0x4c]               | 48 8d 74 24 4c
lea rsi,[rsp+0x50]               | 48 8d 74 24 50
lea rsi,[rsp+0x58]               | 48 8d 74 24 58
lea rsi,[rsp+0x5]                | 48 8d 74 24 05
lea rsi,[rsp+0x5f]               | 48 8d 74 24 5f
lea rsi,[rsp+0x60]               | 48 8d 74 24 60
lea rsi,[rsp+0x68]               | 48 8d 74 24 68
lea rsi,[rsp+0x7]                | 48 8d 74 24 07
lea rsi,[rsp+0x80]               | 48 8d b4 24 80 00 00
lea rsi,[rsp+0x88]               | 48 8d b4 24 88 00 00
lea rsi,[rsp+0x8]                | 48 8d 74 24 08
lea rsi,[rsp+0x98]               | 48 8d b4 24 98 00 00
lea rsi,[rsp+0xa0]               | 48 8d b4 24 a0 00 00
lea rsi,[rsp+0xc0]               | 48 8d b4 24 c0 00 00
lea rsi,[rsp+0xc]                | 48 8d 74 24 0c
lea rsi,[rsp+0xe0]               | 48 8d b4 24 e0 00 00
lea rsi,[rsp+0xf]                | 48 8d 74 24 0f
lea rsp,[rbp-0x18]               | 48 8d 65 e8
lea rsp,[rbp-0x20]               | 48 8d 65 e0
lea rsp,[rbp-0x28]               | 48 8d 65 d8

# Cmove
cmove r15,rax | 4c 0f 44 f8
cmove r15,rbp | 4c 0f 44 fd
cmove r15,rbx | 4c 0f 44 fb
cmove r15,rcx | 4c 0f 44 f9
cmove r15,rdi | 4c 0f 44 ff
cmove r15,rdx | 4c 0f 44 fa
cmove r15,rsi | 4c 0f 44 fe
cmove r15,rsp | 4c 0f 44 fc

# Cmp
cmp rdi,0xfbf | 48 81 ff bf 0f 00 00

# Mov
mov r10d,r10d | 45 89 d2
mov r10d,r11d | 45 89 da
mov r10d,r12d | 45 89 e2
mov r10d,r13d | 45 89 ea
mov r10d,r14d | 45 89 f2
mov r10d,r15d | 45 89 fa
mov r10d,r8d  | 45 89 c2
mov r10d,r9d  | 45 89 ca
mov r11d,r10d | 45 89 d3
mov r11d,r11d | 45 89 db
mov r11d,r12d | 45 89 e3
mov r11d,r13d | 45 89 eb
mov r11d,r14d | 45 89 f3
mov r11d,r15d | 45 89 fb
mov r11d,r8d  | 45 89 c3
mov r11d,r9d  | 45 89 cb
mov r12d,r10d | 45 89 d4
mov r12d,r11d | 45 89 dc
mov r12d,r12d | 45 89 e4
mov r12d,r13d | 45 89 ec
mov r12d,r14d | 45 89 f4
mov r12d,r15d | 45 89 fc
mov r12d,r8d  | 45 89 c4
mov r12d,r9d  | 45 89 cc
mov r13d,r10d | 45 89 d5
mov r13d,r11d | 45 89 dd
mov r13d,r12d | 45 89 e5
mov r13d,r13d | 45 89 ed
mov r13d,r14d | 45 89 f5
mov r13d,r15d | 45 89 fd
mov r13d,r8d  | 45 89 c5
mov r13d,r9d  | 45 89 cd
mov r14d,r10d | 45 89 d6
mov r14d,r11d | 45 89 de
mov r14d,r12d | 45 89 e6
mov r14d,r13d | 45 89 ee
mov r14d,r14d | 45 89 f6
mov r14d,r15d | 45 89 fe
mov r14d,r8d  | 45 89 c6
mov r14d,r9d  | 45 89 ce
mov r15d,r10d | 45 89 d7
mov r15d,r11d | 45 89 df
mov r15d,r12d | 45 89 e7
mov r15d,r13d | 45 89 ef
mov r15d,r14d | 45 89 f7
mov r15d,r15d | 45 89 ff
mov r15d,r8d  | 45 89 c7
mov r15d,r9d  | 45 89 cf
mov r8d,r10d  | 45 89 d0
mov r8d,r11d  | 45 89 d8
mov r8d,r12d  | 45 89 e0
mov r8d,r13d  | 45 89 e8
mov r8d,r14d  | 45 89 f0
mov r8d,r15d  | 45 89 f8
mov r8d,r8d   | 45 89 c0
mov r8d,r9d   | 45 89 c8
mov r9d,r10d  | 45 89 d1
mov r9d,r11d  | 45 89 d9
mov r9d,r12d  | 45 89 e1
mov r9d,r13d  | 45 89 e9
mov r9d,r14d  | 45 89 f1
mov r9d,r15d  | 45 89 f9
mov r9d,r8d   | 45 89 c1
mov r9d,r9d   | 45 89 c9
#
mov eax,eax | 89 c0
mov eax,ebp | 89 e8
mov eax,ebx | 89 d8
mov eax,ecx | 89 c8
mov eax,edi | 89 f8
mov eax,edx | 89 d0
mov eax,esi | 89 f0
mov eax,esp | 89 e0
mov ebp,eax | 89 c5
mov ebp,ebp | 89 ed
mov ebp,ebx | 89 dd
mov ebp,ecx | 89 cd
mov ebp,edi | 89 fd
mov ebp,edx | 89 d5
mov ebp,esi | 89 f5
mov ebp,esp | 89 e5
mov ebx,eax | 89 c3
mov ebx,ebp | 89 eb
mov ebx,ebx | 89 db
mov ebx,ecx | 89 cb
mov ebx,edi | 89 fb
mov ebx,edx | 89 d3
mov ebx,esi | 89 f3
mov ebx,esp | 89 e3
mov ecx,eax | 89 c1
mov ecx,ebp | 89 e9
mov ecx,ebx | 89 d9
mov ecx,ecx | 89 c9
mov ecx,edi | 89 f9
mov ecx,edx | 89 d1
mov ecx,esi | 89 f1
mov ecx,esp | 89 e1
mov edi,eax | 89 c7
mov edi,ebp | 89 ef
mov edi,ebx | 89 df
mov edi,ecx | 89 cf
mov edi,edi | 89 ff
mov edi,edx | 89 d7
mov edi,esi | 89 f7
mov edi,esp | 89 e7
mov edx,eax | 89 c2
mov edx,ebp | 89 ea
mov edx,ebx | 89 da
mov edx,ecx | 89 ca
mov edx,edi | 89 fa
mov edx,edx | 89 d2
mov edx,esi | 89 f2
mov edx,esp | 89 e2
mov esi,eax | 89 c6
mov esi,ebp | 89 ee
mov esi,ebx | 89 de
mov esi,ecx | 89 ce
mov esi,edi | 89 fe
mov esi,edx | 89 d6
mov esi,esi | 89 f6
mov esi,esp | 89 e6
mov esp,eax | 89 c4
mov esp,ebp | 89 ec
mov esp,ebx | 89 dc
mov esp,ecx | 89 cc
mov esp,edi | 89 fc
mov esp,edx | 89 d4
mov esp,esi | 89 f4
mov esp,esp | 89 e4
#
# here we expect to find the values in the same order as the input since
# ByteBuffer is, by default, big-endian
mov eax,0x12345678 | b8 12 34 56 78
mov ebp,0x12345678 | bd 12 34 56 78
mov ebx,0x12345678 | bb 12 34 56 78
mov ecx,0x12345678 | b9 12 34 56 78
mov edi,0x12345678 | bf 12 34 56 78
mov edx,0x12345678 | ba 12 34 56 78
mov esi,0x12345678 | be 12 34 56 78
mov esp,0x12345678 | bc 12 34 56 78
#
mov QWORD PTR [rbp-0xd8],rax | 48 89 85 28 ff ff ff
mov rsi,QWORD PTR [rbp-0xd8] | 48 8b b5 28 ff ff ff

# Test
test rax,rax | 48 85 c0
test rax,rbp | 48 85 e8
test rax,rbx | 48 85 d8
test rax,rcx | 48 85 c8
test rax,rdi | 48 85 f8
test rax,rdx | 48 85 d0
test rax,rsi | 48 85 f0
test rax,rsp | 48 85 e0
test rbp,rax | 48 85 c5
test rbp,rbp | 48 85 ed
test rbp,rbx | 48 85 dd
test rbp,rcx | 48 85 cd
test rbp,rdi | 48 85 fd
test rbp,rdx | 48 85 d5
test rbp,rsi | 48 85 f5
test rbp,rsp | 48 85 e5
test rbx,rax | 48 85 c3
test rbx,rbp | 48 85 eb
test rbx,rbx | 48 85 db
test rbx,rcx | 48 85 cb
test rbx,rdi | 48 85 fb
test rbx,rdx | 48 85 d3
test rbx,rsi | 48 85 f3
test rbx,rsp | 48 85 e3
test rcx,rax | 48 85 c1
test rcx,rbp | 48 85 e9
test rcx,rbx | 48 85 d9
test rcx,rcx | 48 85 c9
test rcx,rdi | 48 85 f9
test rcx,rdx | 48 85 d1
test rcx,rsi | 48 85 f1
test rcx,rsp | 48 85 e1
test rdi,rax | 48 85 c7
test rdi,rbp | 48 85 ef
test rdi,rbx | 48 85 df
test rdi,rcx | 48 85 cf
test rdi,rdi | 48 85 ff
test rdi,rdx | 48 85 d7
test rdi,rsi | 48 85 f7
test rdi,rsp | 48 85 e7
test rdx,rax | 48 85 c2
test rdx,rbp | 48 85 ea
test rdx,rbx | 48 85 da
test rdx,rcx | 48 85 ca
test rdx,rdi | 48 85 fa
test rdx,rdx | 48 85 d2
test rdx,rsi | 48 85 f2
test rdx,rsp | 48 85 e2
test rsi,rax | 48 85 c6
test rsi,rbp | 48 85 ee
test rsi,rbx | 48 85 de
test rsi,rcx | 48 85 ce
test rsi,rdi | 48 85 fe
test rsi,rdx | 48 85 d6
test rsi,rsi | 48 85 f6
test rsi,rsp | 48 85 e6
test rsp,rax | 48 85 c4
test rsp,rbp | 48 85 ec
test rsp,rbx | 48 85 dc
test rsp,rcx | 48 85 cc
test rsp,rdi | 48 85 fc
test rsp,rdx | 48 85 d4
test rsp,rsi | 48 85 f4
test rsp,rsp | 48 85 e4

# Xor
xor r10d,r10d | 45 31 d2
xor r10d,r11d | 45 31 da
xor r10d,r12d | 45 31 e2
xor r10d,r13d | 45 31 ea
xor r10d,r14d | 45 31 f2
xor r10d,r15d | 45 31 fa
xor r10d,r8d  | 45 31 c2
xor r10d,r9d  | 45 31 ca
xor r11d,r10d | 45 31 d3
xor r11d,r11d | 45 31 db
xor r11d,r12d | 45 31 e3
xor r11d,r13d | 45 31 eb
xor r11d,r14d | 45 31 f3
xor r11d,r15d | 45 31 fb
xor r11d,r8d  | 45 31 c3
xor r11d,r9d  | 45 31 cb
xor r12d,r10d | 45 31 d4
xor r12d,r11d | 45 31 dc
xor r12d,r12d | 45 31 e4
xor r12d,r13d | 45 31 ec
xor r12d,r14d | 45 31 f4
xor r12d,r15d | 45 31 fc
xor r12d,r8d  | 45 31 c4
xor r12d,r9d  | 45 31 cc
xor r13d,r10d | 45 31 d5
xor r13d,r11d | 45 31 dd
xor r13d,r12d | 45 31 e5
xor r13d,r13d | 45 31 ed
xor r13d,r14d | 45 31 f5
xor r13d,r15d | 45 31 fd
xor r13d,r8d  | 45 31 c5
xor r13d,r9d  | 45 31 cd
xor r14d,r10d | 45 31 d6
xor r14d,r11d | 45 31 de
xor r14d,r12d | 45 31 e6
xor r14d,r13d | 45 31 ee
xor r14d,r14d | 45 31 f6
xor r14d,r15d | 45 31 fe
xor r14d,r8d  | 45 31 c6
xor r14d,r9d  | 45 31 ce
xor r15d,r10d | 45 31 d7
xor r15d,r11d | 45 31 df
xor r15d,r12d | 45 31 e7
xor r15d,r13d | 45 31 ef
xor r15d,r14d | 45 31 f7
xor r15d,r15d | 45 31 ff
xor r15d,r8d  | 45 31 c7
xor r15d,r9d  | 45 31 cf
xor r8d,r10d  | 45 31 d0
xor r8d,r11d  | 45 31 d8
xor r8d,r12d  | 45 31 e0
xor r8d,r13d  | 45 31 e8
xor r8d,r14d  | 45 31 f0
xor r8d,r15d  | 45 31 f8
xor r8d,r8d   | 45 31 c0
xor r8d,r9d   | 45 31 c8
xor r9d,r10d  | 45 31 d1
xor r9d,r11d  | 45 31 d9
xor r9d,r12d  | 45 31 e1
xor r9d,r13d  | 45 31 e9
xor r9d,r14d  | 45 31 f1
xor r9d,r15d  | 45 31 f9
xor r9d,r8d   | 45 31 c1
xor r9d,r9d   | 45 31 c9
#
xor eax,eax | 31 c0
xor eax,ebp | 31 e8
xor eax,ebx | 31 d8
xor eax,ecx | 31 c8
xor eax,edi | 31 f8
xor eax,edx | 31 d0
xor eax,esi | 31 f0
xor eax,esp | 31 e0
xor ebp,eax | 31 c5
xor ebp,ebp | 31 ed
xor ebp,ebx | 31 dd
xor ebp,ecx | 31 cd
xor ebp,edi | 31 fd
xor ebp,edx | 31 d5
xor ebp,esi | 31 f5
xor ebp,esp | 31 e5
xor ebx,eax | 31 c3
xor ebx,ebp | 31 eb
xor ebx,ebx | 31 db
xor ebx,ecx | 31 cb
xor ebx,edi | 31 fb
xor ebx,edx | 31 d3
xor ebx,esi | 31 f3
xor ebx,esp | 31 e3
xor ecx,eax | 31 c1
xor ecx,ebp | 31 e9
xor ecx,ebx | 31 d9
xor ecx,ecx | 31 c9
xor ecx,edi | 31 f9
xor ecx,edx | 31 d1
xor ecx,esi | 31 f1
xor ecx,esp | 31 e1
xor edi,eax | 31 c7
xor edi,ebp | 31 ef
xor edi,ebx | 31 df
xor edi,ecx | 31 cf
xor edi,edi | 31 ff
xor edi,edx | 31 d7
xor edi,esi | 31 f7
xor edi,esp | 31 e7
xor edx,eax | 31 c2
xor edx,ebp | 31 ea
xor edx,ebx | 31 da
xor edx,ecx | 31 ca
xor edx,edi | 31 fa
xor edx,edx | 31 d2
xor edx,esi | 31 f2
xor edx,esp | 31 e2
xor esi,eax | 31 c6
xor esi,ebp | 31 ee
xor esi,ebx | 31 de
xor esi,ecx | 31 ce
xor esi,edi | 31 fe
xor esi,edx | 31 d6
xor esi,esi | 31 f6
xor esi,esp | 31 e6
xor esp,eax | 31 c4
xor esp,ebp | 31 ec
xor esp,ebx | 31 dc
xor esp,ecx | 31 cc
xor esp,edi | 31 fc
xor esp,edx | 31 d4
xor esp,esi | 31 f4
xor esp,esp | 31 e4

# Add