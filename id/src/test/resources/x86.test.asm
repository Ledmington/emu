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
# - the expected output: the instruction written in human-readable form (intel syntax)
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
# The output of these instructions is different from what you can see from other tools such as objdump
# because here we keep the addition to the instruction pointer implicit.
# In reality, it would look like 'call rip+0x....'
call 0xffffffffffffff18 | e8 18 ff ff ff

# Cdq
cdq | 99

# Ja
# The output of these instructions is different from what you can see from other tools such as objdump
# because here we keep the addition to the instruction pointer implicit.
# In reality, it would look like 'jg rip+0x....'
ja 0x78563412 | 0f 87 12 34 56 78

# Jg
# The output of these instructions is different from what you can see from other tools such as objdump
# because here we keep the addition to the instruction pointer implicit.
# In reality, it would look like 'jg rip+0x....'
jg 0x78563412 | 0f 8f 12 34 56 78

# Je
# The output of these instructions is different from what you can see from other tools such as objdump
# because here we keep the addition to the instruction pointer implicit.
# In reality, it would look like 'je rip+0x....'
je 0x2e0db    | 0f 84 db e0 02 00
je 0x78563412 | 0f 84 12 34 56 78

# Jmp
# The output of these instructions is different from what you can see from other tools such as objdump
# because here we keep the addition to the instruction pointer implicit.
# In reality, it would look like 'jmp rip+0x....'
jmp 0x12       | eb 12
jmp 0x78563412 | e9 12 34 56 78

# Cmove
cmove r15,rcx | 4c 0f 44 f9
cmove rcx,r15 | 49 0f 44 cf

# Cmp
cmp edi,0x12345678 | 81 ff 78 56 34 12
cmp rdi,0xfbf      | 48 81 ff bf 0f 00 00
cmp rsp,r8         | 4c 39 c4

# Lea
lea eax,[r10-0xf]        | 41 8d 42 f1
lea eax,[r10d-0xf]       | 67 41 8d 42 f1
lea eax,[r11+0x1]        | 41 8d 43 01
lea eax,[r11-0x19]       | 41 8d 43 e7
lea eax,[r13*8-0x1]      | 42 8d 04 ed ff ff ff ff
lea eax,[r13+0x12]       | 41 8d 45 12
lea eax,[r8-0xf]         | 41 8d 40 f1
lea eax,[r9+r9-0x1]      | 43 8d 44 09 ff
lea eax,[rax+rax*4+0x1]  | 8d 44 80 01
lea eax,[rax+rax]        | 8d 04 00
lea eax,[rax+rdx-0x71]   | 8d 44 10 8f
lea eax,[rbx+0x1]        | 8d 43 01
lea eax,[rbx+rbx+0x2]    | 8d 44 1b 02
lea eax,[rbx-0x1]        | 8d 43 ff
lea eax,[rcx+0x14]       | 8d 41 14
lea eax,[rcx-0x63]       | 8d 41 9d
lea eax,[rdi*8-0x1]      | 8d 04 fd ff ff ff ff
lea eax,[rdi+0x1]        | 8d 47 01
lea eax,[rdi+rdi-0x1]    | 8d 44 3f ff
lea eax,[rdi-0x824]      | 8d 87 dc f7 ff ff
lea eax,[rdx+0x1]        | 8d 42 01
lea eax,[rdx-0x1c]       | 8d 42 e4
lea eax,[rsi+0x12]       | 8d 46 12
lea eax,[rsi-0x29]       | 8d 46 d7
lea ebp,[r10-0x30]       | 41 8d 6a d0
lea ebp,[r12+0x1]        | 41 8d 6c 24 01
lea ebp,[r15+0x1]        | 41 8d 6f 01
lea ebp,[rax-0x30]       | 8d 68 d0
lea ebp,[rbp+rax+0x1]    | 8d 6c 05 01
lea ebp,[rbx+0x1]        | 8d 6b 01
lea ebp,[rcx+0x1]        | 8d 69 01
lea ebp,[rcx-0xd800]     | 8d a9 00 28 ff ff
lea ebp,[rsi-0x30]       | 8d 6e d0
lea ebx,[r10+0x1]        | 41 8d 5a 01
lea ebx,[r12+0x1]        | 41 8d 5c 24 01
lea ebx,[r12+rax]        | 41 8d 1c 04
lea ebx,[r15+0x2]        | 41 8d 5f 02
lea ebx,[r15-0x1]        | 41 8d 5f ff
lea ebx,[r8-0x1]         | 41 8d 58 ff
lea ebx,[rax+0x1]        | 8d 58 01
lea ebx,[rax+rbx*2]      | 8d 1c 58
lea ebx,[rax-0x61]       | 8d 58 9f
lea ebx,[rdi+rax]        | 8d 1c 07
lea ebx,[rdx+0x1]        | 8d 5a 01
lea ebx,[rdx-0x61]       | 8d 5a 9f
lea ebx,[rsi+0x1]        | 8d 5e 01
lea ebx,[rsi+rbx*2+0x1]  | 8d 5c 5e 01
lea ebx,[rsi-0x1]        | 8d 5e ff
lea ecx,[edx+edx*8]      | 67 8d 0c d2
lea ecx,[r10-0x6]        | 41 8d 4a fa
lea ecx,[r11-0x30]       | 41 8d 4b d0
lea ecx,[r13+r12+0x0]    | 43 8d 4c 25 00
lea ecx,[r14+0x2]        | 41 8d 4e 02
lea ecx,[r14-0x1]        | 41 8d 4e ff
lea ecx,[r15+0x7]        | 41 8d 4f 07
lea ecx,[r8+r11]         | 43 8d 0c 18
lea ecx,[r8-0x1]         | 41 8d 48 ff
lea ecx,[r9+0x2]         | 41 8d 49 02
lea ecx,[r9+r9+0x1]      | 43 8d 4c 09 01
lea ecx,[r9+rax]         | 41 8d 0c 01
lea ecx,[r9-0x44]        | 41 8d 49 bc
lea ecx,[rax+0x80]       | 8d 88 80 00 00 00
lea ecx,[rax+rax]        | 8d 0c 00
lea ecx,[rax+rcx*4-0x37] | 8d 4c 88 c9
lea ecx,[rax-0x72]       | 8d 48 8e
lea ecx,[rbp+0x1c]       | 8d 4d 1c
lea ecx,[rbp+rbp+0x1]    | 8d 4c 2d 01
lea ecx,[rbp-0x45]       | 8d 4d bb
lea ecx,[rbx-0x1]        | 8d 4b ff
lea ecx,[rdi-0x9]        | 8d 4f f7
lea ecx,[rdx+0x8]        | 8d 4a 08
lea ecx,[rdx+rdx*8+0x1]  | 8d 4c d2 01
lea ecx,[rdx+rdx*8]      | 8d 0c d2
lea ecx,[rdx+rdx]        | 8d 0c 12
lea ecx,[rdx+rsi]        | 8d 0c 32
lea ecx,[rdx-0x63]       | 8d 4a 9d
lea ecx,[rsi+0x8]        | 8d 4e 08
lea ecx,[rsi+r8]         | 42 8d 0c 06
lea ecx,[rsi-0x34]       | 8d 4e cc
lea edi,[r10-0x6]        | 41 8d 7a fa
lea edi,[r13+0x1]        | 41 8d 7d 01
lea edi,[r14+0x1]        | 41 8d 7e 01
lea edi,[r14+rax-0x1]    | 41 8d 7c 06 ff
lea edi,[r8+0x2]         | 41 8d 78 02
lea edi,[r8-0x2]         | 41 8d 78 fe
lea edi,[r9-0xc]         | 41 8d 79 f4
lea edi,[rax+0x2]        | 8d 78 02
lea edi,[rbp+0x1]        | 8d 7d 01
lea edi,[rbp+rbx+0x1]    | 8d 7c 1d 01
lea edi,[rbp-0x1]        | 8d 7d ff
lea edi,[rbx+0x1]        | 8d 7b 01
lea edi,[rbx+rbx]        | 8d 3c 1b
lea edi,[rbx-0x2]        | 8d 7b fe
lea edi,[rcx-0x30]       | 8d 79 d0
lea edi,[rsi+0x8]        | 8d 7e 08
lea edi,[rsi-0x2b]       | 8d 7e d5
lea edx,[r10+0x1]        | 41 8d 52 01
lea edx,[r10+r10+0x1]    | 43 8d 54 12 01
lea edx,[r10+r11+0x7]    | 43 8d 54 1a 07
lea edx,[r10+rbp]        | 41 8d 14 2a
lea edx,[r11+0xc0]       | 41 8d 93 c0 00 00 00
lea edx,[r11+rcx-0x37]   | 41 8d 54 0b c9
lea edx,[r12-0x1]        | 41 8d 54 24 ff
lea edx,[r13-0x2]        | 41 8d 55 fe
lea edx,[r14+0x3]        | 41 8d 56 03
lea edx,[r14+rbp]        | 41 8d 14 2e
lea edx,[r14-0x3]        | 41 8d 56 fd
lea edx,[r15+0x1]        | 41 8d 57 01
lea edx,[r8+0xe4]        | 41 8d 90 e4 00 00 00
lea edx,[r8+r8*4]        | 43 8d 14 80
lea edx,[r8-0x9]         | 41 8d 50 f7
lea edx,[r9+0xd8]        | 41 8d 91 d8 00 00 00
lea edx,[r9-0x1]         | 41 8d 51 ff
lea edx,[rax+0x1]        | 8d 50 01
lea edx,[rax+rdi-0x30]   | 8d 54 38 d0
lea edx,[rax-0x61]       | 8d 50 9f
lea edx,[rbp+0x1d]       | 8d 55 1d
lea edx,[rbp-0x2]        | 8d 55 fe
lea edx,[rbx+0x1]        | 8d 53 01
lea edx,[rbx+r12+0xf0]   | 42 8d 94 23 f0 00 00 00
lea edx,[rbx+r12]        | 42 8d 14 23
lea edx,[rbx-0x61]       | 8d 53 9f
lea edx,[rcx+0x1]        | 8d 51 01
lea edx,[rcx+rcx]        | 8d 14 09
lea edx,[rcx+rdx+0x7]    | 8d 54 11 07
lea edx,[rcx-0x49]       | 8d 51 b7
lea edx,[rdi+0x1]        | 8d 57 01
lea edx,[rdi+rcx-0x30]   | 8d 54 0f d0
lea edx,[rdi-0x327]      | 8d 97 d9 fc ff ff
lea edx,[rdx+0x2]        | 8d 52 02
lea edx,[rdx+rdx*2]      | 8d 14 52
lea edx,[rsi+0xcc]       | 8d 96 cc 00 00 00
lea edx,[rsi-0x61]       | 8d 56 9f
lea esi,[r10-0x9]        | 41 8d 72 f7
lea esi,[r11+0x1]        | 41 8d 73 01
lea esi,[r11-0x30]       | 41 8d 73 d0
lea esi,[r12+0x1]        | 41 8d 74 24 01
lea esi,[r12-0x1]        | 41 8d 74 24 ff
lea esi,[r13+0x1]        | 41 8d 75 01
lea esi,[r13-0x1]        | 41 8d 75 ff
lea esi,[r14+0x2]        | 41 8d 76 02
lea esi,[r14-0x2d]       | 41 8d 76 d3
lea esi,[r15+0x1]        | 41 8d 77 01
lea esi,[r15-0x1]        | 41 8d 77 ff
lea esi,[r8-0xc]         | 41 8d 70 f4
lea esi,[r9-0x1f]        | 41 8d 71 e1
lea esi,[rax+0x1]        | 8d 70 01
lea esi,[rax-0x49]       | 8d 70 b7
lea esi,[rbp+0x1]        | 8d 75 01
lea esi,[rbx+0x3b4]      | 8d b3 b4 03 00 00
lea esi,[rbx-0x31]       | 8d 73 cf
lea esi,[rcx+r8]         | 42 8d 34 01
lea esi,[rcx+rcx*2]      | 8d 34 49
lea esi,[rcx+rcx]        | 8d 34 09
lea esi,[rcx-0x30]       | 8d 71 d0
lea esi,[rdi+rdi]        | 8d 34 3f
lea esi,[rdi-0x6]        | 8d 77 fa
lea esi,[rdx+0x30]       | 8d 72 30
lea esi,[rdx-0x5ab]      | 8d b2 55 fa ff ff
lea esi,[rsi+0x1]        | 8d 76 01
lea r10,[r11+0x18]       | 4d 8d 53 18
lea r10,[r12+0x1]        | 4d 8d 54 24 01
lea r10,[r12+rax]        | 4d 8d 14 04
lea r10,[r13+rax+0x0]    | 4d 8d 54 05 00
lea r10,[r13+rdx*8+0x0]  | 4d 8d 54 d5 00
lea r10,[r14+0x1]        | 4d 8d 56 01
lea r10,[r15+r12]        | 4f 8d 14 27
lea r10,[r8+r8*2]        | 4f 8d 14 40
lea r10,[r8+r8]          | 4f 8d 14 00
lea r10,[r9*8+0x0]       | 4e 8d 14 cd 00 00 00 00
lea r10,[r9+0x8]         | 4d 8d 51 08
lea r10,[r9+r8]          | 4f 8d 14 01
lea r10,[r9+r9*2]        | 4f 8d 14 49
lea r10,[r9+r9]          | 4f 8d 14 09
lea r10,[rax+0x1]        | 4c 8d 50 01
lea r10,[rax+r8]         | 4e 8d 14 00
lea r10,[rax-0x2]        | 4c 8d 50 fe
lea r10,[rbp+0x12]       | 4c 8d 55 12
lea r10,[rbp+r15+0x0]    | 4e 8d 54 3d 00
lea r10,[rbx+0x4]        | 4c 8d 53 04
lea r10,[rbx+rax]        | 4c 8d 14 03
lea r10,[rcx*8+0x17]     | 4c 8d 14 cd 17 00 00 00
lea r10,[rip+0x91290]    | 4c 8d 15 90 12 09 00
lea r10,[rsi+0x8]        | 4c 8d 56 08
lea r10,[rsp+0x3e8]      | 4c 8d 94 24 e8 03 00 00
lea r10d,[r10+rax]       | 45 8d 14 02
lea r10d,[r10+rdx]       | 45 8d 14 12
lea r10d,[r11+0x2]       | 45 8d 53 02
lea r10d,[r12+r12+0x2]   | 47 8d 54 24 02
lea r10d,[r12+r8-0x30]   | 47 8d 54 04 d0
lea r10d,[r12+rbx]       | 45 8d 14 1c
lea r10d,[r14+rdx+0x3]   | 45 8d 54 16 03
lea r10d,[r14-0x4]       | 45 8d 56 fc
lea r10d,[r8+0x3]        | 45 8d 50 03
lea r10d,[rax-0x1]       | 44 8d 50 ff
lea r10d,[rbp+0x57]      | 44 8d 55 57
lea r10d,[rbx-0x1]       | 44 8d 53 ff
lea r10d,[rcx+0x1]       | 44 8d 51 01
lea r10d,[rcx+rax]       | 44 8d 14 01
lea r10d,[rcx-0x30]      | 44 8d 51 d0
lea r10d,[rdi+0x1]       | 44 8d 57 01
lea r10d,[rdx+0x1]       | 44 8d 52 01
lea r10d,[rdx-0x4]       | 44 8d 52 fc
lea r11,[r10+0x10]       | 4d 8d 5a 10
lea r11,[r10+rsi*8-0x10] | 4d 8d 5c f2 f0
lea r11,[r12+0x58]       | 4d 8d 5c 24 58
lea r11,[r12+rbp]        | 4d 8d 1c 2c
lea r11,[r13+0x70]       | 4d 8d 5d 70
lea r11,[r13+r13+0x0]    | 4f 8d 5c 2d 00
lea r11,[r14+0x48baa0]   | 4d 8d 9e a0 ba 48 00
lea r11,[r14+r12]        | 4f 8d 1c 26
lea r11,[r15+0x8]        | 4d 8d 5f 08
lea r11,[r15+r10]        | 4f 8d 1c 17
lea r11,[r8+r9-0x18]     | 4f 8d 5c 08 e8
lea r11,[r8+rax]         | 4d 8d 1c 00
lea r11,[r9+0x4]         | 4d 8d 59 04
lea r11,[r9+r9*2]        | 4f 8d 1c 49
lea r11,[r9+rax]         | 4d 8d 1c 01
lea r11,[rax+0x8]        | 4c 8d 58 08
lea r11,[rbp+r10+0x10]   | 4e 8d 5c 15 10
lea r11,[rbx+rbx]        | 4c 8d 1c 1b
lea r11,[rcx+0x8]        | 4c 8d 59 08
lea r11,[rdx+0x1]        | 4c 8d 5a 01
lea r11,[rdx+rsi*8+0x8]  | 4c 8d 5c f2 08
lea r11,[rsp+0x6f0]      | 4c 8d 9c 24 f0 06 00 00
lea r11d,[r13-0x5]       | 45 8d 5d fb
lea r11d,[r15*8+0x0]     | 46 8d 1c fd 00 00 00 00
lea r11d,[r15-0x825]     | 45 8d 9f db f7 ff ff
lea r11d,[r8+0x2af]      | 45 8d 98 af 02 00 00
lea r11d,[r8+rcx]        | 45 8d 1c 08
lea r11d,[r9+0x2]        | 45 8d 59 02
lea r11d,[rax-0x61]      | 44 8d 58 9f
lea r11d,[rbx+0x14]      | 44 8d 5b 14
lea r11d,[rcx+r8]        | 46 8d 1c 01
lea r11d,[rcx-0x1]       | 44 8d 59 ff
lea r11d,[rdi+0x2]       | 44 8d 5f 02
lea r11d,[rdi-0xc]       | 44 8d 5f f4
lea r11d,[rdx+0x57]      | 44 8d 5a 57
lea r12,[r11+0xf]        | 4d 8d 63 0f
lea r12,[r12+rax+0x1]    | 4d 8d 64 04 01
lea r12,[r13+0x1]        | 4d 8d 65 01
lea r12,[r13+r11+0x0]    | 4f 8d 64 1d 00
lea r12,[r14+0x8]        | 4d 8d 66 08
lea r12,[r15+0xb]        | 4d 8d 67 0b
lea r12,[r15+r10+0x18]   | 4f 8d 64 17 18
lea r12,[r15+r11*8+0x18] | 4f 8d 64 df 18
lea r12,[r8+0x1]         | 4d 8d 60 01
lea r12,[r9+rcx]         | 4d 8d 24 09
lea r12,[rax*4+0x4]      | 4c 8d 24 85 04 00 00 00
lea r12,[rax+0x18]       | 4c 8d 60 18
lea r12,[rax+rbx*8]      | 4c 8d 24 d8
lea r12,[rax+rbx]        | 4c 8d 24 18
lea r12,[rbp*8+0x8]      | 4c 8d 24 ed 08 00 00 00
lea r12,[rbp+0x1]        | 4c 8d 65 01
lea r12,[rbp+rdx+0x0]    | 4c 8d 64 15 00
lea r12,[rbp-0x2b0]      | 4c 8d a5 50 fd ff ff
lea r12,[rbx+0xb]        | 4c 8d 63 0b
lea r12,[rcx+0x2]        | 4c 8d 61 02
lea r12,[rcx+r15]        | 4e 8d 24 39
lea r12,[rdi*8+0x17]     | 4c 8d 24 fd 17 00 00 00
lea r12,[rdi+0x10]       | 4c 8d 67 10
lea r12,[rdi+rsi-0x1]    | 4c 8d 64 37 ff
lea r12,[rdx+0x10]       | 4c 8d 62 10
lea r12,[rdx+rcx*8]      | 4c 8d 24 ca
lea r12,[rdx+rdi]        | 4c 8d 24 3a
lea r12,[rdx-0x1]        | 4c 8d 62 ff
lea r12,[rip+0x8c4e9]    | 4c 8d 25 e9 c4 08 00
lea r12,[rsi+rbx]        | 4c 8d 24 1e
lea r12,[rsp+0x58]       | 4c 8d 64 24 58
lea r12d,[r10-0x30]      | 45 8d 62 d0
lea r12d,[r11+0x1]       | 45 8d 63 01
lea r12d,[r12+0x1]       | 45 8d 64 24 01
lea r12d,[r13-0x9]       | 45 8d 65 f7
lea r12d,[r14-0x2]       | 45 8d 66 fe
lea r12d,[r8+rsi]        | 45 8d 24 30
lea r12d,[rax+0x1]       | 44 8d 60 01
lea r12d,[rbp+0x1]       | 44 8d 65 01
lea r12d,[rbx+0x1e]      | 44 8d 63 1e
lea r12d,[rbx-0x1]       | 44 8d 63 ff
lea r12d,[rcx+0x1]       | 44 8d 61 01
lea r12d,[rsi+0x1]       | 44 8d 66 01
lea r13,[r10+0x8]        | 4d 8d 6a 08
lea r13,[r11+0x17]       | 4d 8d 6b 17
lea r13,[r12+0x58]       | 4d 8d 6c 24 58
lea r13,[r12+rax]        | 4d 8d 2c 04
lea r13,[r12-0x1]        | 4d 8d 6c 24 ff
lea r13,[r14+0x2]        | 4d 8d 6e 02
lea r13,[r14+r12]        | 4f 8d 2c 26
lea r13,[r14+rax]        | 4d 8d 2c 06
lea r13,[r15+0xd8]       | 4d 8d af d8 00 00 00
lea r13,[r9+0x20]        | 4d 8d 69 20
lea r13,[rax+0x1]        | 4c 8d 68 01
lea r13,[rax+r14]        | 4e 8d 2c 30
lea r13,[rax+r9*8-0x10]  | 4e 8d 6c c8 f0
lea r13,[rax+rbx-0x1]    | 4c 8d 6c 18 ff
lea r13,[rbp+0x8]        | 4c 8d 6d 08
lea r13,[rbp+r14+0x0]    | 4e 8d 6c 35 00
lea r13,[rbp-0x220]      | 4c 8d ad e0 fd ff ff
lea r13,[rbx+0x10]       | 4c 8d 6b 10
lea r13,[rbx+rax]        | 4c 8d 2c 03
lea r13,[rbx-0x1]        | 4c 8d 6b ff
lea r13,[rcx+0x4e2600]   | 4c 8d a9 00 26 4e 00
lea r13,[rcx+rax]        | 4c 8d 2c 01
lea r13,[rcx+rsi]        | 4c 8d 2c 31
lea r13,[rdi+0x78]       | 4c 8d 6f 78
lea r13,[rdx+r8]         | 4e 8d 2c 02
lea r13,[rip+0x79a08]    | 4c 8d 2d 08 9a 07 00
lea r13,[rsi+r9*8+0x8]   | 4e 8d 6c ce 08
lea r13,[rsp+0xc0]       | 4c 8d ac 24 c0 00 00 00
lea r13d,[r10-0x1]       | 45 8d 6a ff
lea r13d,[r13+0x1]       | 45 8d 6d 01
lea r13d,[r14+0x1]       | 45 8d 6e 01
lea r13d,[r15+0x1]       | 45 8d 6f 01
lea r13d,[r9+0x1]        | 45 8d 69 01
lea r13d,[rcx+0x2]       | 44 8d 69 02
lea r13d,[rsi+rsi]       | 44 8d 2c 36
lea r13d,[rsi-0x1]       | 44 8d 6e ff
lea r14,[r11+0x14]       | 4d 8d 73 14
lea r14,[r11+r9-0x1]     | 4f 8d 74 0b ff
lea r14,[r12+r9*8]       | 4f 8d 34 cc
lea r14,[r12+rax+0x8]    | 4d 8d 74 04 08
lea r14,[r13+0xd0]       | 4d 8d b5 d0 00 00 00
lea r14,[r13+r8+0x10]    | 4f 8d 74 05 10
lea r14,[r14+0x8]        | 4d 8d 76 08
lea r14,[r14+r15*8+0x8]  | 4f 8d 74 fe 08
lea r14,[r14+r15+0x1]    | 4f 8d 74 3e 01
lea r14,[r15+0x1]        | 4d 8d 77 01
lea r14,[r15+r13]        | 4f 8d 34 2f
lea r14,[r15+r9-0x9]     | 4f 8d 74 0f f7
lea r14,[r8+0x10]        | 4d 8d 70 10
lea r14,[r9+0x2]         | 4d 8d 71 02
lea r14,[rax+0x48ad60]   | 4c 8d b0 60 ad 48 00
lea r14,[rax+r15-0x1]    | 4e 8d 74 38 ff
lea r14,[rax-0x2]        | 4c 8d 70 fe
lea r14,[rbp+0x4]        | 4c 8d 75 04
lea r14,[rbp+rax-0x20]   | 4c 8d 74 05 e0
lea r14,[rbp-0x130]      | 4c 8d b5 d0 fe ff ff
lea r14,[rbx+0x1d]       | 4c 8d 73 1d
lea r14,[rdi+0x5]        | 4c 8d 77 05
lea r14,[rdx+0x15b0]     | 4c 8d b2 b0 15 00 00
lea r14,[rip+0x77a58]    | 4c 8d 35 58 7a 07 00
lea r14,[rsi+r14+0x1]    | 4e 8d 74 36 01
lea r14,[rsi+rax+0x64]   | 4c 8d 74 06 64
lea r14,[rsi+rbx-0x10]   | 4c 8d 74 1e f0
lea r14,[rsp+0x88]       | 4c 8d b4 24 88 00 00 00
lea r14d,[r12+0x1]       | 45 8d 74 24 01
lea r14d,[r12-0x1]       | 45 8d 74 24 ff
lea r14d,[r14+rbx-0x1]   | 45 8d 74 1e ff
lea r14d,[r14+rdx+0x4]   | 45 8d 74 16 04
lea r14d,[r14-0x2]       | 45 8d 76 fe
lea r14d,[r15+r12]       | 47 8d 34 27
lea r14d,[r15-0x1]       | 45 8d 77 ff
lea r14d,[r8+0x2]        | 45 8d 70 02
lea r14d,[r9-0x3]        | 45 8d 71 fd
lea r14d,[rax+0x1]       | 44 8d 70 01
lea r14d,[rax-0x1]       | 44 8d 70 ff
lea r14d,[rbp+0x1]       | 44 8d 75 01
lea r14d,[rdx+0x4]       | 44 8d 72 04
lea r14d,[rsi+0x1]       | 44 8d 76 01
lea r15,[r10+0x4]        | 4d 8d 7a 04
lea r15,[r11+0x1]        | 4d 8d 7b 01
lea r15,[r12+0x1]        | 4d 8d 7c 24 01
lea r15,[r12+r14-0x10]   | 4f 8d 7c 34 f0
lea r15,[r13+0x2]        | 4d 8d 7d 02
lea r15,[r14+0x8]        | 4d 8d 7e 08
lea r15,[r15+0x1]        | 4d 8d 7f 01
lea r15,[r15+rsi]        | 4d 8d 3c 37
lea r15,[r8+rax+0x1]     | 4d 8d 7c 00 01
lea r15,[r9+0x48baa0]    | 4d 8d b9 a0 ba 48 00
lea r15,[rax+0x1]        | 4c 8d 78 01
lea r15,[rbp+0x2]        | 4c 8d 7d 02
lea r15,[rbp-0x3a0]      | 4c 8d bd 60 fc ff ff
lea r15,[rbx+0x80]       | 4c 8d bb 80 00 00 00
lea r15,[rbx+r11]        | 4e 8d 3c 1b
lea r15,[rcx+0x1]        | 4c 8d 79 01
lea r15,[rcx+rsi-0x8]    | 4c 8d 7c 31 f8
lea r15,[rdx+0x1278]     | 4c 8d ba 78 12 00 00
lea r15,[rsi+0x1]        | 4c 8d 7e 01
lea r15,[rsp+0x150]      | 4c 8d bc 24 50 01 00 00
lea r15d,[r14-0x1]       | 45 8d 7e ff
lea r15d,[r9*4+0x0]      | 46 8d 3c 8d 00 00 00 00
lea r15d,[rbp+0x1e]      | 44 8d 7d 1e
lea r15d,[rbp-0x1]       | 44 8d 7d ff
lea r15d,[rcx+rcx]       | 44 8d 3c 09
lea r15d,[rdx+0x1]       | 44 8d 7a 01
lea r8,[r10+0x1]         | 4d 8d 42 01
lea r8,[r11+0x17]        | 4d 8d 43 17
lea r8,[r11+rbp]         | 4d 8d 04 2b
lea r8,[r12+0x78]        | 4d 8d 44 24 78
lea r8,[r12+r13]         | 4f 8d 04 2c
lea r8,[r13+r12+0x0]     | 4f 8d 44 25 00
lea r8,[r13+r13*2+0x0]   | 4f 8d 44 6d 00
lea r8,[r13-0x8]         | 4d 8d 45 f8
lea r8,[r14*8+0x0]       | 4e 8d 04 f5 00 00 00 00
lea r8,[r14+rbx]         | 4d 8d 04 1e
lea r8,[r15+0x38]        | 4d 8d 47 38
lea r8,[r15+r12]         | 4f 8d 04 27
lea r8,[r15+rcx*8]       | 4d 8d 04 cf
lea r8,[r15+rdi*8]       | 4d 8d 04 ff
lea r8,[r8+0x4]          | 4d 8d 40 04
lea r8,[rax+0x80]        | 4c 8d 80 80 00 00 00
lea r8,[rax+rdi-0x1]     | 4c 8d 44 38 ff
lea r8,[rax+rdi]         | 4c 8d 04 38
lea r8,[rax-0x1]         | 4c 8d 40 ff
lea r8,[rbp+0xb40]       | 4c 8d 85 40 0b 00 00
lea r8,[rbp-0xc0]        | 4c 8d 85 40 ff ff ff
lea r8,[rbx*8+0x0]       | 4c 8d 04 dd 00 00 00 00
lea r8,[rbx+0x58]        | 4c 8d 43 58
lea r8,[rbx+rbx*2]       | 4c 8d 04 5b
lea r8,[rcx*4+0x17]      | 4c 8d 04 8d 17 00 00 00
lea r8,[rcx+0x1]         | 4c 8d 41 01
lea r8,[rcx+rax]         | 4c 8d 04 01
lea r8,[rcx+rsi-0x18]    | 4c 8d 44 31 e8
lea r8,[rdi+0x8]         | 4c 8d 47 08
lea r8,[rdi+r9+0x1]      | 4e 8d 44 0f 01
lea r8,[rdx*8+0xf]       | 4c 8d 04 d5 0f 00 00 00
lea r8,[rdx+0x18]        | 4c 8d 42 18
lea r8,[rdx+rax+0x3]     | 4c 8d 44 02 03
lea r8,[rdx+rcx]         | 4c 8d 04 0a
lea r8,[rip+0xa22da]     | 4c 8d 05 da 22 0a 00
lea r8,[rsi*8+0x17]      | 4c 8d 04 f5 17 00 00 00
lea r8,[rsi+0x10]        | 4c 8d 46 10
lea r8,[rsi+rax+0x1]     | 4c 8d 44 06 01
lea r8,[rsi+rsi*2]       | 4c 8d 04 76
lea r8,[rsp+0xe8]        | 4c 8d 84 24 e8 00 00 00
lea r8,[rsp+r12+0x4b0]   | 4e 8d 84 24 b0 04 00 00
lea r8d,[r10+0x1]        | 45 8d 42 01
lea r8d,[r10+r11]        | 47 8d 04 1a
lea r8d,[r11+0x8]        | 45 8d 43 08
lea r8d,[r11+r9*4+0x1]   | 47 8d 44 8b 01
lea r8d,[r13+r13+0x1]    | 47 8d 44 2d 01
lea r8d,[r14+0x1]        | 45 8d 46 01
lea r8d,[r14+r11*8+0x3]  | 47 8d 44 de 03
lea r8d,[r14-0x3]        | 45 8d 46 fd
lea r8d,[r15+0x3]        | 45 8d 47 03
lea r8d,[r9+0x2]         | 45 8d 41 02
lea r8d,[rax*8+0x0]      | 44 8d 04 c5 00 00 00 00
lea r8d,[rax+0x4]        | 44 8d 40 04
lea r8d,[rax+rdx*2]      | 44 8d 04 50
lea r8d,[rax+rdx-0x1]    | 44 8d 44 10 ff
lea r8d,[rax-0x1]        | 44 8d 40 ff
lea r8d,[rbx+0x1000]     | 44 8d 83 00 10 00 00
lea r8d,[rcx-0x41]       | 44 8d 41 bf
lea r8d,[rdi+0x6]        | 44 8d 47 06
lea r8d,[rdi-0x4ef]      | 44 8d 87 11 fb ff ff
lea r8d,[rdx+rax]        | 44 8d 04 02
lea r8d,[rdx+rdx+0x2]    | 44 8d 44 12 02
lea r8d,[rdx-0x30]       | 44 8d 42 d0
lea r9,[r10*8+0x0]       | 4e 8d 0c d5 00 00 00 00
lea r9,[r11+r11*2]       | 4f 8d 0c 5b
lea r9,[r12+0x3]         | 4d 8d 4c 24 03
lea r9,[r12+r14]         | 4f 8d 0c 34
lea r9,[r12+rdi]         | 4d 8d 0c 3c
lea r9,[r13*4+0x0]       | 4e 8d 0c ad 00 00 00 00
lea r9,[r13+0x1]         | 4d 8d 4d 01
lea r9,[r13+rcx+0x0]     | 4d 8d 4c 0d 00
lea r9,[r13+rdi*8+0x0]   | 4d 8d 4c fd 00
lea r9,[r14+0x17]        | 4d 8d 4e 17
lea r9,[r14+rbx]         | 4d 8d 0c 1e
lea r9,[r15*4+0x0]       | 4e 8d 0c bd 00 00 00 00
lea r9,[r15+rax*8]       | 4d 8d 0c c7
lea r9,[r8+0x48baa0]     | 4d 8d 88 a0 ba 48 00
lea r9,[r8-0x18]         | 4d 8d 48 e8
lea r9,[rax+0x8]         | 4c 8d 48 08
lea r9,[rax+r12+0x10]    | 4e 8d 4c 20 10
lea r9,[rax+rax*2]       | 4c 8d 0c 40
lea r9,[rax+rsi]         | 4c 8d 0c 30
lea r9,[rbp+0x4e6030]    | 4c 8d 8d 30 60 4e 00
lea r9,[rbx+0x60]        | 4c 8d 4b 60
lea r9,[rbx+rdx+0xc]     | 4c 8d 4c 13 0c
lea r9,[rcx+r12+0x8]     | 4e 8d 4c 21 08
lea r9,[rcx+r8+0x8]      | 4e 8d 4c 01 08
lea r9,[rcx+rdi+0x8]     | 4c 8d 4c 39 08
lea r9,[rdi+0x3]         | 4c 8d 4f 03
lea r9,[rdi+r8*8]        | 4e 8d 0c c7
lea r9,[rdi+r8]          | 4e 8d 0c 07
lea r9,[rdi+rbx]         | 4c 8d 0c 1f
lea r9,[rdx+0x8]         | 4c 8d 4a 08
lea r9,[rip+0x9417e]     | 4c 8d 0d 7e 41 09 00
lea r9,[rsi+0x8]         | 4c 8d 4e 08
lea r9,[rsi+rsi*2]       | 4c 8d 0c 76
lea r9,[rsp+0xc8]        | 4c 8d 8c 24 c8 00 00 00
lea r9d,[r10-0xc]        | 45 8d 4a f4
lea r9d,[r11+0x2]        | 45 8d 4b 02
lea r9d,[r12-0x1]        | 45 8d 4c 24 ff
lea r9d,[r13+0x1]        | 45 8d 4d 01
lea r9d,[r13+rax+0x0]    | 45 8d 4c 05 00
lea r9d,[r14+0x2]        | 45 8d 4e 02
lea r9d,[r14-0x4]        | 45 8d 4e fc
lea r9d,[r15+0x1]        | 45 8d 4f 01
lea r9d,[r8+0x2]         | 45 8d 48 02
lea r9d,[r8-0x6]         | 45 8d 48 fa
lea r9d,[rax+0x1]        | 44 8d 48 01
lea r9d,[rax+r8]         | 46 8d 0c 00
lea r9d,[rbx-0x1]        | 44 8d 4b ff
lea r9d,[rcx-0x41]       | 44 8d 49 bf
lea r9d,[rdi-0x1]        | 44 8d 4f ff
lea r9d,[rsi+0x2]        | 44 8d 4e 02
lea r9d,[rsi-0x29]       | 44 8d 4e d7
lea rax,[r10+r10*2]      | 4b 8d 04 52
lea rax,[r11+0x17]       | 49 8d 43 17
lea rax,[r11+rcx]        | 49 8d 04 0b
lea rax,[r12+0x8]        | 49 8d 44 24 08
lea rax,[r12-0x1]        | 49 8d 44 24 ff
lea rax,[r13+0x5]        | 49 8d 45 05
lea rax,[r14+0x1]        | 49 8d 46 01
lea rax,[r14+r8]         | 4b 8d 04 06
lea rax,[r14+rax+0x1]    | 49 8d 44 06 01
lea rax,[r14+rbx]        | 49 8d 04 1e
lea rax,[r15+0xd]        | 49 8d 47 0d
lea rax,[r8+0x8]         | 49 8d 40 08
lea rax,[r8+r8*2]        | 4b 8d 04 40
lea rax,[r8+r9]          | 4b 8d 04 08
lea rax,[r9+0x2]         | 49 8d 41 02
lea rax,[rax*8+0xf]      | 48 8d 04 c5 0f 00 00 00
lea rax,[rax+0x1]        | 48 8d 40 01
lea rax,[rax+rax*4]      | 48 8d 04 80
lea rax,[rax+rcx+0x3]    | 48 8d 44 08 03
lea rax,[rax+rdx-0x1]    | 48 8d 44 10 ff
lea rax,[rbp+0x58]       | 48 8d 45 58
lea rax,[rbp+rax-0x1]    | 48 8d 44 05 ff
lea rax,[rbp+rbp*2+0x0]  | 48 8d 44 6d 00
lea rax,[rbp+rbp*4+0x0]  | 48 8d 44 ad 00
lea rax,[rbp+rcx*8+0x0]  | 48 8d 44 cd 00
lea rax,[rbp-0x824]      | 48 8d 85 dc f7 ff ff
lea rax,[rbx+0x80]       | 48 8d 83 80 00 00 00
lea rax,[rbx+rax+0x1]    | 48 8d 44 03 01
lea rax,[rbx+rsi]        | 48 8d 04 33
lea rax,[rbx-0x20]       | 48 8d 43 e0
lea rax,[rcx+0x2]        | 48 8d 41 02
lea rax,[rcx+r8-0x20]    | 4a 8d 44 01 e0
lea rax,[rcx+r8]         | 4a 8d 04 01
lea rax,[rcx+rax+0x10]   | 48 8d 44 01 10
lea rax,[rcx+rsi]        | 48 8d 04 31
lea rax,[rdi+0x18]       | 48 8d 47 18
lea rax,[rdi+r15]        | 4a 8d 04 3f
lea rax,[rdi-0x18]       | 48 8d 47 e8
lea rax,[rdx+0x8]        | 48 8d 42 08
lea rax,[rdx+r14]        | 4a 8d 04 32
lea rax,[rdx+rax-0x1]    | 48 8d 44 02 ff
lea rax,[rdx+rdi+0x3]    | 48 8d 44 3a 03
lea rax,[rdx+rdx*2]      | 48 8d 04 52
lea rax,[rdx+rdx]        | 48 8d 04 12
lea rax,[rip+0x95255]    | 48 8d 05 55 52 09 00
lea rax,[rip-0x1203]     | 48 8d 05 fd ed ff ff
lea rax,[rsi+0x8]        | 48 8d 46 08
lea rax,[rsi+rax+0x1]    | 48 8d 44 06 01
lea rax,[rsi+rcx]        | 48 8d 04 0e
lea rax,[rsi+rdx]        | 48 8d 04 16
lea rax,[rsi+rsi]        | 48 8d 04 36
lea rax,[rsp+0xf]        | 48 8d 44 24 0f
lea rbp,[r11+0x4cb760]   | 49 8d ab 60 b7 4c 00
lea rbp,[r12+0x1]        | 49 8d 6c 24 01
lea rbp,[r12+r15]        | 4b 8d 2c 3c
lea rbp,[r13+0x1]        | 49 8d 6d 01
lea rbp,[r13+rax+0x0]    | 49 8d 6c 05 00
lea rbp,[r13+rdx+0x8]    | 49 8d 6c 15 08
lea rbp,[r14+0x3]        | 49 8d 6e 03
lea rbp,[r8+0x48baa0]    | 49 8d a8 a0 ba 48 00
lea rbp,[r9+0x58]        | 49 8d 69 58
lea rbp,[rax*8+0x10]     | 48 8d 2c c5 10 00 00 00
lea rbp,[rax+0x60]       | 48 8d 68 60
lea rbp,[rax+r14+0x2]    | 4a 8d 6c 30 02
lea rbp,[rbp+0x25]       | 48 8d 6d 25
lea rbp,[rbp+rbx-0x8]    | 48 8d 6c 1d f8
lea rbp,[rbx+0x1]        | 48 8d 6b 01
lea rbp,[rbx+rcx]        | 48 8d 2c 0b
lea rbp,[rbx-0xc]        | 48 8d 6b f4
lea rbp,[rcx+0x10000]    | 48 8d a9 00 00 01 00
lea rbp,[rcx-0x1]        | 48 8d 69 ff
lea rbp,[rdi+0x80]       | 48 8d af 80 00 00 00
lea rbp,[rdi+r15]        | 4a 8d 2c 3f
lea rbp,[rdi+rsi]        | 48 8d 2c 37
lea rbp,[rip+0x784dc]    | 48 8d 2d dc 84 07 00
lea rbp,[rip-0x180c]     | 48 8d 2d f4 e7 ff ff
lea rbp,[rsi+0x20]       | 48 8d 6e 20
lea rbp,[rsi+rax]        | 48 8d 2c 06
lea rbp,[rsi+rdx]        | 48 8d 2c 16
lea rbp,[rsp+0x98]       | 48 8d ac 24 98 00 00 00
lea rbx,[r11+0x17]       | 49 8d 5b 17
lea rbx,[r11+r14+0x8]    | 4b 8d 5c 33 08
lea rbx,[r12+0x1]        | 49 8d 5c 24 01
lea rbx,[r12+rax]        | 49 8d 1c 04
lea rbx,[r13+0x20]       | 49 8d 5d 20
lea rbx,[r13+r13*4+0x5]  | 4b 8d 5c ad 05
lea rbx,[r13+r8*8+0x0]   | 4b 8d 5c c5 00
lea rbx,[r13-0xc]        | 49 8d 5d f4
lea rbx,[r14+0x1]        | 49 8d 5e 01
lea rbx,[r15-0x1]        | 49 8d 5f ff
lea rbx,[r8+rbx]         | 49 8d 1c 18
lea rbx,[r9+r14+0x8]     | 4b 8d 5c 31 08
lea rbx,[r9+rdx]         | 49 8d 1c 11
lea rbx,[rax+0x18]       | 48 8d 58 18
lea rbx,[rax+rdx-0x1]    | 48 8d 5c 10 ff
lea rbx,[rbp+0xa]        | 48 8d 5d 0a
lea rbx,[rbp-0x2a0]      | 48 8d 9d 60 fd ff ff
lea rbx,[rbx*4+0x4]      | 48 8d 1c 9d 04 00 00 00
lea rbx,[rbx+0x1]        | 48 8d 5b 01
lea rbx,[rbx+rax+0x1]    | 48 8d 5c 03 01
lea rbx,[rbx+rbp+0x4]    | 48 8d 5c 2b 04
lea rbx,[rcx+0x1]        | 48 8d 59 01
lea rbx,[rcx+r14-0x30]   | 4a 8d 5c 31 d0
lea rbx,[rdi*8+0x0]      | 48 8d 1c fd 00 00 00 00
lea rbx,[rdi+0x9]        | 48 8d 5f 09
lea rbx,[rdx+r15]        | 4a 8d 1c 3a
lea rbx,[rip+0x9062d]    | 48 8d 1d 2d 06 09 00
lea rbx,[rsi+r14-0x30]   | 4a 8d 5c 36 d0
lea rbx,[rsi-0x1]        | 48 8d 5e ff
lea rbx,[rsp+0x6f0]      | 48 8d 9c 24 f0 06 00 00
lea rcx,[eax+r12d]       | 67 4a 8d 0c 20
lea rcx,[edx+edx*8]      | 67 48 8d 0c d2
lea rcx,[esp+eax]        | 67 48 8d 0c 04
lea rcx,[r10+0x1]        | 49 8d 4a 01
lea rcx,[r11+0x48baa0]   | 49 8d 8b a0 ba 48 00
lea rcx,[r11+r8]         | 4b 8d 0c 03
lea rcx,[r12+0x40]       | 49 8d 4c 24 40
lea rcx,[r13+0x1]        | 49 8d 4d 01
lea rcx,[r13+r10+0x0]    | 4b 8d 4c 15 00
lea rcx,[r13+rax+0x0]    | 49 8d 4c 05 00
lea rcx,[r13+rdx+0x0]    | 49 8d 4c 15 00
lea rcx,[r14+0x1]        | 49 8d 4e 01
lea rcx,[r14+r12]        | 4b 8d 0c 26
lea rcx,[r14-0x18]       | 49 8d 4e e8
lea rcx,[r14]            | 49 8d 0e
lea rcx,[r15+rax]        | 49 8d 0c 07
lea rcx,[r15+rcx*8]      | 49 8d 0c cf
lea rcx,[r8+0x8]         | 49 8d 48 08
lea rcx,[r8+rax-0x8]     | 49 8d 4c 00 f8
lea rcx,[r9+0x30]        | 49 8d 49 30
lea rcx,[rax*8+0x0]      | 48 8d 0c c5 00 00 00 00
lea rcx,[rax+0x60]       | 48 8d 48 60
lea rcx,[rax+r12]        | 4a 8d 0c 20
lea rcx,[rax+r14*4]      | 4a 8d 0c b0
lea rcx,[rax+r8+0x1]     | 4a 8d 4c 00 01
lea rcx,[rax+rax*2]      | 48 8d 0c 40
lea rcx,[rax+rdx]        | 48 8d 0c 10
lea rcx,[rax+rsi]        | 48 8d 0c 30
lea rcx,[rax-0x8]        | 48 8d 48 f8
lea rcx,[rbp*8+0x0]      | 48 8d 0c ed 00 00 00 00
lea rcx,[rbp+0xb38]      | 48 8d 8d 38 0b 00 00
lea rcx,[rbp+r12*8+0x0]  | 4a 8d 4c e5 00
lea rcx,[rbp+rcx+0x8]    | 48 8d 4c 0d 08
lea rcx,[rbp+rdi+0x0]    | 48 8d 4c 3d 00
lea rcx,[rbp-0xe0]       | 48 8d 8d 20 ff ff ff
lea rcx,[rbx+0xc]        | 48 8d 4b 0c
lea rcx,[rbx-0x50]       | 48 8d 4b b0
lea rcx,[rcx+rbp*8]      | 48 8d 0c e9
lea rcx,[rcx+rcx*2]      | 48 8d 0c 49
lea rcx,[rcx-0x1]        | 48 8d 49 ff
lea rcx,[rdi+0x48baa0]   | 48 8d 8f a0 ba 48 00
lea rcx,[rdx*8+0x0]      | 48 8d 0c d5 00 00 00 00
lea rcx,[rdx+0x2]        | 48 8d 4a 02
lea rcx,[rdx+rax-0x1]    | 48 8d 4c 02 ff
lea rcx,[rdx+rax]        | 48 8d 0c 02
lea rcx,[rdx+rdx*2]      | 48 8d 0c 52
lea rcx,[rdx+rdx*8]      | 48 8d 0c d2
lea rcx,[rip+0x8c78f]    | 48 8d 0d 8f c7 08 00
lea rcx,[rsi+0x1]        | 48 8d 4e 01
lea rcx,[rsp+0x4b0]      | 48 8d 8c 24 b0 04 00 00
lea rdi,[r10+0x5]        | 49 8d 7a 05
lea rdi,[r10+r13+0x1]    | 4b 8d 7c 2a 01
lea rdi,[r11+0xcc]       | 49 8d bb cc 00 00 00
lea rdi,[r11+r12]        | 4b 8d 3c 23
lea rdi,[r11+rax]        | 49 8d 3c 03
lea rdi,[r11-0x2]        | 49 8d 7b fe
lea rdi,[r12+0xb0]       | 49 8d bc 24 b0 00 00 00
lea rdi,[r12+r10]        | 4b 8d 3c 14
lea rdi,[r12+r14+0x8]    | 4b 8d 7c 34 08
lea rdi,[r12+r9]         | 4b 8d 3c 0c
lea rdi,[r12+rax*4]      | 49 8d 3c 84
lea rdi,[r12+rax+0x1]    | 49 8d 7c 04 01
lea rdi,[r12+rbp]        | 49 8d 3c 2c
lea rdi,[r13+0xcc]       | 49 8d bd cc 00 00 00
lea rdi,[r13+r12+0x0]    | 4b 8d 7c 25 00
lea rdi,[r13+r15*8+0x0]  | 4b 8d 7c fd 00
lea rdi,[r13+rbx*8+0x0]  | 49 8d 7c dd 00
lea rdi,[r13+rdx+0x0]    | 49 8d 7c 15 00
lea rdi,[r14+0x7]        | 49 8d 7e 07
lea rdi,[r14+r12]        | 4b 8d 3c 26
lea rdi,[r14+rax+0x32]   | 49 8d 7c 06 32
lea rdi,[r14+rax-0x6]    | 49 8d 7c 06 fa
lea rdi,[r14+rbx]        | 49 8d 3c 1e
lea rdi,[r14+rsi+0x9]    | 49 8d 7c 36 09
lea rdi,[r15*8+0x17]     | 4a 8d 3c fd 17 00 00 00
lea rdi,[r15+0x5]        | 49 8d 7f 05
lea rdi,[r15+r10]        | 4b 8d 3c 17
lea rdi,[r15+r15]        | 4b 8d 3c 3f
lea rdi,[r15+rax*8]      | 49 8d 3c c7
lea rdi,[r15+rax]        | 49 8d 3c 07
lea rdi,[r15+rbp]        | 49 8d 3c 2f
lea rdi,[r15+rbx*8]      | 49 8d 3c df
lea rdi,[r15+rdi*8]      | 49 8d 3c ff
lea rdi,[r15+rsi*8]      | 49 8d 3c f7
lea rdi,[r15-0x2]        | 49 8d 7f fe
lea rdi,[r8+0x8]         | 49 8d 78 08
lea rdi,[r8+r13]         | 4b 8d 3c 28
lea rdi,[r8+rax+0x3]     | 49 8d 7c 00 03
lea rdi,[r8+rax]         | 49 8d 3c 00
lea rdi,[r8+rsi*8+0x8]   | 49 8d 7c f0 08
lea rdi,[r9+0xcc]        | 49 8d b9 cc 00 00 00
lea rdi,[r9+r10]         | 4b 8d 3c 11
lea rdi,[r9+r13]         | 4b 8d 3c 29
lea rdi,[r9+r8+0x3]      | 4b 8d 7c 01 03
lea rdi,[r9+r8]          | 4b 8d 3c 01
lea rdi,[r9+rax]         | 49 8d 3c 01
lea rdi,[r9+rcx*8]       | 49 8d 3c c9
lea rdi,[r9+rsi]         | 49 8d 3c 31
lea rdi,[rax*8+0x0]      | 48 8d 3c c5 00 00 00 00
lea rdi,[rax+0xd0]       | 48 8d b8 d0 00 00 00
lea rdi,[rax+r12+0x1]    | 4a 8d 7c 20 01
lea rdi,[rax+r13+0x1]    | 4a 8d 7c 28 01
lea rdi,[rax+r13]        | 4a 8d 3c 28
lea rdi,[rax+r14+0x1]    | 4a 8d 7c 30 01
lea rdi,[rax+r15+0x8]    | 4a 8d 7c 38 08
lea rdi,[rax+r15]        | 4a 8d 3c 38
lea rdi,[rax+r8+0x20]    | 4a 8d 7c 00 20
lea rdi,[rax+rbp+0x3]    | 48 8d 7c 28 03
lea rdi,[rax+rbp]        | 48 8d 3c 28
lea rdi,[rax+rdx+0xe6c]  | 48 8d bc 10 6c 0e 00 00
lea rdi,[rax-0x18]       | 48 8d 78 e8
lea rdi,[rbp+0xb40]      | 48 8d bd 40 0b 00 00
lea rdi,[rbp+r13+0x18]   | 4a 8d 7c 2d 18
lea rdi,[rbp+r14+0x0]    | 4a 8d 7c 35 00
lea rdi,[rbp+r8+0x0]     | 4a 8d 7c 05 00
lea rdi,[rbp+rax+0x0]    | 48 8d 7c 05 00
lea rdi,[rbp-0x210]      | 48 8d bd f0 fd ff ff
lea rdi,[rbx+0x8]        | 48 8d 7b 08
lea rdi,[rbx+r12]        | 4a 8d 3c 23
lea rdi,[rbx+r13]        | 4a 8d 3c 2b
lea rdi,[rbx+r8+0x8]     | 4a 8d 7c 03 08
lea rdi,[rbx+rdx]        | 48 8d 3c 13
lea rdi,[rbx-0x18]       | 48 8d 7b e8
lea rdi,[rcx+0xc]        | 48 8d 79 0c
lea rdi,[rcx+r12+0x10]   | 4a 8d 7c 21 10
lea rdi,[rcx+rax*4]      | 48 8d 3c 81
lea rdi,[rcx+rax]        | 48 8d 3c 01
lea rdi,[rcx+rdx]        | 48 8d 3c 11
lea rdi,[rcx+rsi*8-0x10] | 48 8d 7c f1 f0
lea rdi,[rdi+0x40]       | 48 8d 7f 40
lea rdi,[rdi+rax+0x3]    | 48 8d 7c 07 03
lea rdi,[rdx+0x8]        | 48 8d 7a 08
lea rdi,[rdx+r10+0x8]    | 4a 8d 7c 12 08
lea rdi,[rdx+rax-0x1]    | 48 8d 7c 02 ff
lea rdi,[rdx+rax]        | 48 8d 3c 02
lea rdi,[rdx+rcx]        | 48 8d 3c 0a
lea rdi,[rdx+rsi]        | 48 8d 3c 32
lea rdi,[rdx-0x18]       | 48 8d 7a e8
lea rdi,[rip+0x9fc4a]    | 48 8d 3d 4a fc 09 00
lea rdi,[rip-0x11bc]     | 48 8d 3d 44 ee ff ff
lea rdi,[rsi+0x8]        | 48 8d 7e 08
lea rdi,[rsi+rax+0x1]    | 48 8d 7c 06 01
lea rdi,[rsi+rdi*8]      | 48 8d 3c fe
lea rdi,[rsp+0x6f0]      | 48 8d bc 24 f0 06 00 00
lea rdi,[rsp+r11*8+0x50] | 4a 8d 7c dc 50
lea rdx,[r10+0x1]        | 49 8d 52 01
lea rdx,[r10+rsi]        | 49 8d 14 32
lea rdx,[r11+0x78]       | 49 8d 53 78
lea rdx,[r11+r13]        | 4b 8d 14 2b
lea rdx,[r11+r15]        | 4b 8d 14 3b
lea rdx,[r11+r8]         | 4b 8d 14 03
lea rdx,[r11+r9]         | 4b 8d 14 0b
lea rdx,[r11+rdx+0x1]    | 49 8d 54 13 01
lea rdx,[r12+0x2]        | 49 8d 54 24 02
lea rdx,[r12+r11+0x1]    | 4b 8d 54 1c 01
lea rdx,[r12-0x8]        | 49 8d 54 24 f8
lea rdx,[r13+0x58]       | 49 8d 55 58
lea rdx,[r13+rax+0x0]    | 49 8d 54 05 00
lea rdx,[r13+rcx+0x1]    | 49 8d 54 0d 01
lea rdx,[r13-0x1]        | 49 8d 55 ff
lea rdx,[r14+0x1]        | 49 8d 56 01
lea rdx,[r14-0x1]        | 49 8d 56 ff
lea rdx,[r15+0x18]       | 49 8d 57 18
lea rdx,[r15+r11*8]      | 4b 8d 14 df
lea rdx,[r15+r12]        | 4b 8d 14 27
lea rdx,[r15+rax]        | 49 8d 14 07
lea rdx,[r15-0x1]        | 49 8d 57 ff
lea rdx,[r8+0x90]        | 49 8d 90 90 00 00 00
lea rdx,[r8+rax+0x3]     | 49 8d 54 00 03
lea rdx,[r8-0x1]         | 49 8d 50 ff
lea rdx,[r9+0x1]         | 49 8d 51 01
lea rdx,[rax*8+0x0]      | 48 8d 14 c5 00 00 00 00
lea rdx,[rax+0x8]        | 48 8d 50 08
lea rdx,[rax+r8]         | 4a 8d 14 00
lea rdx,[rax+rbp]        | 48 8d 14 28
lea rdx,[rax+rcx*8]      | 48 8d 14 c8
lea rdx,[rax-0x18]       | 48 8d 50 e8
lea rdx,[rbp*8+0x0]      | 48 8d 14 ed 00 00 00 00
lea rdx,[rbp+0xb4]       | 48 8d 95 b4 00 00 00
lea rdx,[rbp+r12+0x0]    | 4a 8d 54 25 00
lea rdx,[rbp+rax+0x0]    | 48 8d 54 05 00
lea rdx,[rbp+rbx+0x0]    | 48 8d 54 1d 00
lea rdx,[rbp+rcx+0xa6a]  | 48 8d 94 0d 6a 0a 00 00
lea rdx,[rbp+rdi+0xc6c]  | 48 8d 94 3d 6c 0c 00 00
lea rdx,[rbp-0x228]      | 48 8d 95 d8 fd ff ff
lea rdx,[rbx*8+0x0]      | 48 8d 14 dd 00 00 00 00
lea rdx,[rbx+0x78]       | 48 8d 53 78
lea rdx,[rbx+rbx*2]      | 48 8d 14 5b
lea rdx,[rbx+rbx]        | 48 8d 14 1b
lea rdx,[rbx+rsi]        | 48 8d 14 33
lea rdx,[rbx-0x8]        | 48 8d 53 f8
lea rdx,[rcx+0x8]        | 48 8d 51 08
lea rdx,[rcx+r8+0x644]   | 4a 8d 94 01 44 06 00 00
lea rdx,[rdi+0x70]       | 48 8d 57 70
lea rdx,[rdx+rax+0xa68]  | 48 8d 94 02 68 0a 00 00
lea rdx,[rip+0x9554b]    | 48 8d 15 4b 55 09 00
lea rdx,[rip-0x286]      | 48 8d 15 7a fd ff ff
lea rdx,[rsi*4+0x0]      | 48 8d 14 b5 00 00 00 00
lea rdx,[rsi*8+0x0]      | 48 8d 14 f5 00 00 00 00
lea rdx,[rsi+0x38]       | 48 8d 56 38
lea rdx,[rsi+r8]         | 4a 8d 14 06
lea rdx,[rsi+rsi]        | 48 8d 14 36
lea rdx,[rsi-0x1]        | 48 8d 56 ff
lea rdx,[rsp+0x390]      | 48 8d 94 24 90 03 00 00
lea rsi,[r10*4+0x8]      | 4a 8d 34 95 08 00 00 00
lea rsi,[r10*8+0x0]      | 4a 8d 34 d5 00 00 00 00
lea rsi,[r10+0x1]        | 49 8d 72 01
lea rsi,[r11*8+0x8]      | 4a 8d 34 dd 08 00 00 00
lea rsi,[r11+0xcc]       | 49 8d b3 cc 00 00 00
lea rsi,[r12+0xa8]       | 49 8d b4 24 a8 00 00 00
lea rsi,[r12+rax+0x18]   | 49 8d 74 04 18
lea rsi,[r12+rax-0x2]    | 49 8d 74 04 fe
lea rsi,[r12+rbx]        | 49 8d 34 1c
lea rsi,[r13+0x18]       | 49 8d 75 18
lea rsi,[r13+r10*8+0x0]  | 4b 8d 74 d5 00
lea rsi,[r13+r15+0x0]    | 4b 8d 74 3d 00
lea rsi,[r13+r9+0x0]     | 4b 8d 74 0d 00
lea rsi,[r13+rax+0x4]    | 49 8d 74 05 04
lea rsi,[r13+rbx*8+0x8]  | 49 8d 74 dd 08
lea rsi,[r13+rcx*8+0x8]  | 49 8d 74 cd 08
lea rsi,[r14+r15]        | 4b 8d 34 3e
lea rsi,[r14+rax+0x2]    | 49 8d 74 06 02
lea rsi,[r14+rbx-0x3]    | 49 8d 74 1e fd
lea rsi,[r14-0x1]        | 49 8d 76 ff
lea rsi,[r15+r10*8+0x80] | 4b 8d b4 d7 80 00 00 00
lea rsi,[r15+r12]        | 4b 8d 34 27
lea rsi,[r15+rbp+0x8]    | 49 8d 74 2f 08
lea rsi,[r15+rsi*8]      | 49 8d 34 f7
lea rsi,[r15-0x1]        | 49 8d 77 ff
lea rsi,[r8*8+0x8]       | 4a 8d 34 c5 08 00 00 00
lea rsi,[r8+0xcc]        | 49 8d b0 cc 00 00 00
lea rsi,[r8+r10-0x1]     | 4b 8d 74 10 ff
lea rsi,[r8+rcx]         | 49 8d 34 08
lea rsi,[r8-0x1]         | 49 8d 70 ff
lea rsi,[r9*8+0x0]       | 4a 8d 34 cd 00 00 00 00
lea rsi,[r9+0xf]         | 49 8d 71 0f
lea rsi,[r9+r13]         | 4b 8d 34 29
lea rsi,[r9+r8]          | 4b 8d 34 01
lea rsi,[r9+rax+0x4]     | 49 8d 74 01 04
lea rsi,[rax*4+0x0]      | 48 8d 34 85 00 00 00 00
lea rsi,[rax*8+0x8]      | 48 8d 34 c5 08 00 00 00
lea rsi,[rax+0x48baa0]   | 48 8d b0 a0 ba 48 00
lea rsi,[rax+r10*8-0x10] | 4a 8d 74 d0 f0
lea rsi,[rax+r9+0x2]     | 4a 8d 74 08 02
lea rsi,[rax+rbx]        | 48 8d 34 18
lea rsi,[rax+rcx]        | 48 8d 34 08
lea rsi,[rax+rdx]        | 48 8d 34 10
lea rsi,[rax-0x1]        | 48 8d 70 ff
lea rsi,[rbp*8+0x8]      | 48 8d 34 ed 08 00 00 00
lea rsi,[rbp+0x40]       | 48 8d 75 40
lea rsi,[rbp+r13-0x1]    | 4a 8d 74 2d ff
lea rsi,[rbp+r8+0x0]     | 4a 8d 74 05 00
lea rsi,[rbp+r9+0x0]     | 4a 8d 74 0d 00
lea rsi,[rbp+rax+0x0]    | 48 8d 74 05 00
lea rsi,[rbp+rbx+0x0]    | 48 8d 74 1d 00
lea rsi,[rbp-0xc8]       | 48 8d b5 38 ff ff ff
lea rsi,[rbx+0x48baa0]   | 48 8d b3 a0 ba 48 00
lea rsi,[rbx+r10+0x8]    | 4a 8d 74 13 08
lea rsi,[rbx+r15+0x8]    | 4a 8d 74 3b 08
lea rsi,[rbx+rax+0x4]    | 48 8d 74 03 04
lea rsi,[rbx+rdx]        | 48 8d 34 13
lea rsi,[rbx-0xc]        | 48 8d 73 f4
lea rsi,[rcx*8+0x8]      | 48 8d 34 cd 08 00 00 00
lea rsi,[rcx+0x70]       | 48 8d 71 70
lea rsi,[rcx+r8]         | 4a 8d 34 01
lea rsi,[rcx+rax+0x3]    | 48 8d 74 01 03
lea rsi,[rcx+rbx+0x28]   | 48 8d 74 19 28
lea rsi,[rcx+rbx-0x1]    | 48 8d 74 19 ff
lea rsi,[rcx+rdx+0x1]    | 48 8d 74 11 01
lea rsi,[rcx-0x1]        | 48 8d 71 ff
lea rsi,[rdi*8+0x8]      | 48 8d 34 fd 08 00 00 00
lea rsi,[rdi+0x20]       | 48 8d 77 20
lea rsi,[rdi+r10*8]      | 4a 8d 34 d7
lea rsi,[rdi+r11]        | 4a 8d 34 1f
lea rsi,[rdi+r8*8]       | 4a 8d 34 c7
lea rsi,[rdi+r8]         | 4a 8d 34 07
lea rsi,[rdi+r9]         | 4a 8d 34 0f
lea rsi,[rdi+rax]        | 48 8d 34 07
lea rsi,[rdi+rcx]        | 48 8d 34 0f
lea rsi,[rdi+rdx]        | 48 8d 34 17
lea rsi,[rdi-0x825]      | 48 8d b7 db f7 ff ff
lea rsi,[rdx*8+0x17]     | 48 8d 34 d5 17 00 00 00
lea rsi,[rdx+0x188]      | 48 8d b2 88 01 00 00
lea rsi,[rdx+rbx]        | 48 8d 34 1a
lea rsi,[rdx+rcx*8-0x10] | 48 8d 74 ca f0
lea rsi,[rdx+rdx*4]      | 48 8d 34 92
lea rsi,[rdx-0x7]        | 48 8d 72 f9
lea rsi,[rip+0xe490]     | 48 8d 35 90 e4 00 00
lea rsi,[rip-0x9b3a]     | 48 8d 35 c6 64 ff ff
lea rsi,[rsi*8+0x8]      | 48 8d 34 f5 08 00 00 00
lea rsi,[rsi+rdi]        | 48 8d 34 3e
lea rsi,[rsi+rdx]        | 48 8d 34 16
lea rsi,[rsi-0x1]        | 48 8d 76 ff
lea rsi,[rsp+0xe0]       | 48 8d b4 24 e0 00 00 00
lea rsp,[rbp-0x28]       | 48 8d 65 d8

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
mov QWORD PTR [rbp+0x7eadbeef],0x12345678 | 48 c7 85 ef be ad 7e 78 56 34 12
mov QWORD PTR [rbp-0xf0],0x0              | 48 c7 85 10 ff ff ff 00 00 00 00

# Push
push r10 | 41 52
push r11 | 41 53
push r12 | 41 54
push r13 | 41 55
push r14 | 41 56
push r15 | 41 57
push r8  | 41 50
push r9  | 41 51
push rax | 50
push rbp | 55
push rbx | 53
push rcx | 51
push rdi | 57
push rdx | 52
push rsi | 56
push rsp | 54

# Pop
pop r10 | 41 5a
pop r11 | 41 5b
pop r12 | 41 5c
pop r13 | 41 5d
pop r14 | 41 5e
pop r15 | 41 5f
pop r8  | 41 58
pop r9  | 41 59
pop rax | 58
pop rbp | 5d
pop rbx | 5b
pop rcx | 59
pop rdi | 5f
pop rdx | 5a
pop rsi | 5e
pop rsp | 5c

# Leave
leave | c9

# Ret
ret | c3

# Add
add eax,0x18 | 83 c0 18
add r8,0x1   | 49 83 c0 01
add rax,0x1  | 48 83 c0 01

# And
and edi,0x1                | 83 e7 01
and edi,0x1d               | 83 e7 1d
and edi,0x7f               | 83 e7 7f
and edi,0xf                | 83 e7 0f
and r15d,0x1f              | 41 83 e7 1f
and rdi,0xfffffffffffffff0 | 48 83 e7 f0

# Sub
sub rsp,0x12345678 | 48 81 ec 78 56 34 12

# Or
or QWORD PTR [rsp+0xff8],0x0                   | 48 83 8c 24 f8 0f 00 00 00
