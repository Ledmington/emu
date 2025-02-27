#
# Input file for x86 (64-bit) instruction decoding
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
#
nop ax  | 66 0f 1f c0
nop eax | 0f 1f c0
nop rax | 48 0f 1f c0
#
nop DWORD PTR [eax]                    | 67 0f 1f 00
nop DWORD PTR [rax]                    | 0f 1f 00
nop DWORD PTR [rbx+r12*4+0x12345678]   | 42 0f 1f 84 a3 78 56 34 12
nop QWORD PTR [eax]                    | 67 48 0f 1f 00
nop QWORD PTR [rax]                    | 48 0f 1f 00
nop QWORD PTR [rbx+r12*4+0x12345678]   | 4a 0f 1f 84 a3 78 56 34 12
nop WORD PTR [eax]                     | 67 66 0f 1f 00
nop WORD PTR [rax]                     | 66 0f 1f 00
nop WORD PTR [rbx+r12*4+0x12345678]    | 66 42 0f 1f 84 a3 78 56 34 12
nop WORD PTR cs:[rbx+r12*4+0x12345678] | 2e 66 42 0f 1f 84 a3 78 56 34 12

# Call
# The output of these instructions is different from what you can see from other tools such as objdump
# because here we keep the addition to the instruction pointer implicit.
# In reality, it would look like 'call rip+0x....'
call 0x12345678         | e8 78 56 34 12
call 0xfffffffff8563412 | e8 12 34 56 f8
call 0xffffffffffffff18 | e8 18 ff ff ff
# the following ones are calls with registers (as offsets?)
call r10 | 41 ff d2
call r11 | 41 ff d3
call r12 | 41 ff d4
call r13 | 41 ff d5
call r14 | 41 ff d6
call r15 | 41 ff d7
call r8  | 41 ff d0
call r9  | 41 ff d1
call rax | ff d0
call rbp | ff d5
call rbx | ff d3
call rcx | ff d1
call rdi | ff d7
call rdx | ff d2
call rsi | ff d6
call rsp | ff d4
#
call DWORD PTR [ebx]                    | 66 67 ff 1b
call DWORD PTR [r11+r12*4+0x12345678]   | 66 43 ff 9c a3 78 56 34 12
call DWORD PTR [r11d+r12d*4+0x12345678] | 67 66 43 ff 9c a3 78 56 34 12
call DWORD PTR [rsp]                    | 66 ff 1c 24
call QWORD PTR [eax]                    | 67 ff 10
call QWORD PTR [r11+r12*4+0x12345678]   | 43 ff 94 a3 78 56 34 12
call QWORD PTR [rdx]                    | ff 12
call WORD PTR [ecx]                     | 67 66 ff 11
call WORD PTR [r11+r12*4+0x12345678]    | 66 43 ff 94 a3 78 56 34 12
call WORD PTR [r11d+r12d*4+0x12345678]  | 67 66 43 ff 94 a3 78 56 34 12
call WORD PTR [rsi]                     | 66 ff 16

# Cdq
cdq | 99

# Cwde
cwde | 98

# Cdqe
cdqe | 48 98

#
### Jumps
# The output of these instructions is different from what you can see from other tools such as objdump
# because here we keep the addition to the instruction pointer implicit.
# In reality, it would look like 'jXX rip+0x....'
# Ja
ja 0x12       | 77 12
ja 0x12345678 | 0f 87 78 56 34 12
# Jae
jae 0x12       | 73 12
jae 0x12345678 | 0f 83 78 56 34 12
# Jb
jb 0x12       | 72 12
jb 0x12345678 | 0f 82 78 56 34 12
# Jbe
jbe 0x12       | 76 12
jbe 0x12345678 | 0f 86 78 56 34 12
# Jg
jg 0x12       | 7f 12
jg 0x12345678 | 0f 8f 78 56 34 12
# Je
je 0x12       | 74 12
je 0x12345678 | 0f 84 78 56 34 12
# Jl
jl 0x12       | 7c 12
jl 0x12345678 | 0f 8c 78 56 34 12
# Jle
jle 0x12       | 7e 12
jle 0x12345678 | 0f 8e 78 56 34 12
# Jge
jge 0x12       | 7d 12
jge 0x12345678 | 0f 8d 78 56 34 12
# Jne
jne 0x12       | 75 12
jne 0x12345678 | 0f 85 78 56 34 12
# Jns
jns 0x12       | 79 12
jns 0x12345678 | 0f 89 78 56 34 12
# Js
js 0x12       | 78 12
js 0x12345678 | 0f 88 78 56 34 12
# Jp
jp 0x12       | 7a 12
jp 0x12345678 | 0f 8a 78 56 34 12
# Jmp
jmp 0x12       | eb 12
jmp 0x12345678 | e9 78 56 34 12
#
jmp ax   | 66 ff e0
jmp r11  | 41 ff e3
jmp r11w | 66 41 ff e3
jmp rax  | ff e0
#
jmp DWORD PTR [r11]  | 66 41 ff 2b
jmp DWORD PTR [r11d] | 67 66 41 ff 2b
jmp QWORD PTR [r11]  | 41 ff 23
jmp QWORD PTR [r11d] | 67 41 ff 23
jmp WORD PTR [r11]   | 66 41 ff 23
jmp WORD PTR [r11d]  | 67 66 41 ff 23
#
jmp DWORD PTR [eax+ecx*4+0x12345678] | 67 66 ff ac 88 78 56 34 12
jmp DWORD PTR [rax+rcx*4+0x12345678] | 66 ff ac 88 78 56 34 12
jmp QWORD PTR [eax+ecx*4+0x12345678] | 67 ff a4 88 78 56 34 12
jmp QWORD PTR [rax+rcx*4+0x12345678] | ff a4 88 78 56 34 12
jmp WORD PTR [eax+ecx*4+0x12345678]  | 67 66 ff a4 88 78 56 34 12
jmp WORD PTR [rax+rcx*4+0x12345678]  | 66 ff a4 88 78 56 34 12

# Cmove
cmove ecx,DWORD PTR [r8+rax*4+0x12345678] | 41 0f 44 8c 80 78 56 34 12
cmove r15,rcx                             | 4c 0f 44 f9
cmove rcx,r15                             | 49 0f 44 cf

# Cmovns
cmovns ecx,DWORD PTR [r8+rax*4+0x12345678] | 41 0f 49 8c 80 78 56 34 12
cmovns r15,rcx                             | 4c 0f 49 f9
cmovns rcx,r15                             | 49 0f 49 cf

# Cmovae
cmovae ecx,DWORD PTR [r8+rax*4+0x12345678] | 41 0f 43 8c 80 78 56 34 12
cmovae r15,rcx                             | 4c 0f 43 f9
cmovae rcx,r15                             | 49 0f 43 cf

# Cmovb
cmovb ecx,DWORD PTR [r8+rax*4+0x12345678] | 41 0f 42 8c 80 78 56 34 12
cmovb r15,rcx                             | 4c 0f 42 f9
cmovb rcx,r15                             | 49 0f 42 cf

# Cmovbe
cmovbe ecx,DWORD PTR [r8+rax*4+0x12345678] | 41 0f 46 8c 80 78 56 34 12
cmovbe r15,rcx                             | 4c 0f 46 f9
cmovbe rcx,r15                             | 49 0f 46 cf

# Cmovne
cmovne ecx,DWORD PTR [r8+rax*4+0x12345678] | 41 0f 45 8c 80 78 56 34 12
cmovne r15,rdx                             | 4c 0f 45 fa
cmovne rdx,r15                             | 49 0f 45 d7

# Cmovg
cmovg ecx,DWORD PTR [r8+rax*4+0x12345678] | 41 0f 4f 8c 80 78 56 34 12
cmovg r15,rdx                             | 4c 0f 4f fa
cmovg rdx,r15                             | 49 0f 4f d7

# Cmovge
cmovge ecx,DWORD PTR [r8+rax*4+0x12345678] | 41 0f 4d 8c 80 78 56 34 12
cmovge r15,rdx                             | 4c 0f 4d fa
cmovge rdx,r15                             | 49 0f 4d d7

# Cmovs
cmovs ecx,DWORD PTR [r8+rax*4+0x12345678] | 41 0f 48 8c 80 78 56 34 12
cmovs ecx,eax                             | 0f 48 c8
cmovs edx,r9d                             | 41 0f 48 d1

# Cmova
cmova ecx,DWORD PTR [r8+rax*4+0x12345678] | 41 0f 47 8c 80 78 56 34 12
cmova ecx,eax                             | 0f 47 c8
cmova edx,r9d                             | 41 0f 47 d1

# Cmovl
cmovl ecx,DWORD PTR [r8+rax*4+0x12345678] | 41 0f 4c 8c 80 78 56 34 12
cmovl r15,rdx                             | 4c 0f 4c fa
cmovl rdx,r15                             | 49 0f 4c d7

# Cmovle
cmovle ecx,DWORD PTR [r8+rax*4+0x12345678] | 41 0f 4e 8c 80 78 56 34 12
cmovle r15,rdx                             | 4c 0f 4e fa
cmovle rdx,r15                             | 49 0f 4e d7

# Cmp
cmp BYTE PTR [eax],dh                          | 67 38 30
cmp BYTE PTR [edi],0x77                        | 67 80 3f 77
cmp BYTE PTR [r13+rcx*2+0x12],0x77             | 41 80 7c 4d 12 77
cmp BYTE PTR [r9+rcx*4+0x12345678],0x99        | 41 80 bc 89 78 56 34 12 99
cmp BYTE PTR [rbx+r9*4+0x12345678],r9b         | 46 38 8c 8b 78 56 34 12
cmp BYTE PTR [rdi],0x77                        | 80 3f 77
cmp DWORD PTR [ebp-0xe8],r15d                  | 67 44 39 bd 18 ff ff ff
cmp DWORD PTR [edi],0x12345678                 | 67 81 3f 78 56 34 12
cmp DWORD PTR [r13+rcx*2+0x12],0x66778899      | 41 81 7c 4d 12 99 88 77 66
cmp DWORD PTR [r9+rcx*4+0x12345678],0xdeadbeef | 41 81 bc 89 78 56 34 12 ef be ad de
cmp DWORD PTR [rbp-0xe8],r15d                  | 44 39 bd 18 ff ff ff
cmp DWORD PTR [rdi],0x12345678                 | 81 3f 78 56 34 12
cmp QWORD PTR [edi],0x0000000012345678         | 67 48 81 3f 78 56 34 12
cmp QWORD PTR [rdi],0x0000000012345678         | 48 81 3f 78 56 34 12
cmp WORD PTR [edi],0x7788                      | 67 66 81 3f 88 77
cmp WORD PTR [r13+rcx*2+0x12],0x0077           | 66 41 83 7c 4d 12 77
cmp WORD PTR [r13+rcx*2+0x12],0x7788           | 66 41 81 7c 4d 12 88 77
cmp WORD PTR [r9+rcx*4+0x12345678],0xbeef      | 66 41 81 bc 89 78 56 34 12 ef be
cmp WORD PTR [rdi],0x7788                      | 66 81 3f 88 77
#
cmp dh,BYTE PTR [eax]                   | 67 3a 30
cmp dh,BYTE PTR [rax]                   | 3a 30
cmp dx,WORD PTR [eax]                   | 67 66 3b 10
cmp dx,WORD PTR [rax]                   | 66 3b 10
cmp ebp,DWORD PTR [rbx+r9*4+0x12345678] | 42 3b ac 8b 78 56 34 12
cmp edx,DWORD PTR [eax]                 | 67 3b 10
cmp edx,DWORD PTR [rax]                 | 3b 10
cmp rdx,QWORD PTR [eax]                 | 67 48 3b 10
cmp rdx,QWORD PTR [rax]                 | 48 3b 10
#
cmp al,0x99                | 3c 99
cmp al,dh                  | 38 f0
cmp cx,0x1234              | 66 81 f9 34 12
cmp dh,0x99                | 80 fe 99
cmp eax,0x12345678         | 3d 78 56 34 12
cmp edi,0x12345678         | 81 ff 78 56 34 12
cmp esp,r13d               | 44 39 ec
cmp r8b,0x12               | 41 80 f8 12
cmp r8w,dx                 | 66 41 39 d0
cmp rax,0x0000000012345678 | 48 3d 78 56 34 12
cmp rdi,0x0000000012345678 | 48 81 ff 78 56 34 12
cmp rsp,r8                 | 4c 39 c4
cmp sp,r13w                | 66 44 39 ec

# Lea
lea ax,[ebx+ecx*4+0x12345678]   | 67 66 8d 84 8b 78 56 34 12
lea cx,[rbx+rcx*4+0x12345678]   | 66 8d 8c 8b 78 56 34 12
lea eax,[ebx]                   | 67 8d 03
lea eax,[rbx]                   | 8d 03
lea ecx,[rdx+rbp*2+0x0]         | 8d 0c 6a
lea esi,[edi+r12d*2+0x12345678] | 67 42 8d b4 67 78 56 34 12
lea r10w,[ebx+ecx*4+0x12345678] | 66 67 44 8d 94 8b 78 56 34 12
lea r10w,[ebx+ecx*4+0x12345678] | 67 66 44 8d 94 8b 78 56 34 12
lea r13d,[rdi+r8*4+0x12345678]  | 46 8d ac 87 78 56 34 12
lea r14w,[rbx+rcx*4+0x12345678] | 66 44 8d b4 8b 78 56 34 12
lea r9d,[edx+ebp*2+0x0]         | 67 44 8d 0c 6a
lea rax,[ebx]                   | 67 48 8d 03
lea rax,[rbx]                   | 48 8d 03
lea rcx,[edx+ebp*2+0x0]         | 67 48 8d 0c 6a
lea rcx,[rdx+rbp*2+0x0]         | 48 8d 0c 6a
lea rsi,[edi+r9d*2+0x12345678]  | 67 4a 8d b4 4f 78 56 34 12
lea rsi,[rdi+r8*4+0x12345678]   | 4a 8d b4 87 78 56 34 12

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
mov rax,rbx | 48 89 d8
mov rcx,rdx | 48 89 d1
mov rsi,rdi | 48 89 fe
mov rsp,rax | 48 89 c4
mov rsp,rbp | 48 89 ec
mov rsp,rbx | 48 89 dc
mov rsp,rcx | 48 89 cc
mov rsp,rdi | 48 89 fc
mov rsp,rdx | 48 89 d4
mov rsp,rsi | 48 89 f4
mov rsp,rsp | 48 89 e4
#
mov BYTE PTR [r11+r8*4+0x12345678],0x99        | 43 c6 84 83 78 56 34 12 99
mov BYTE PTR [rdi],bl                          | 88 1f
mov BYTE PTR [rsp+rcx*4+0x12345678],bh         | 88 bc 8c 78 56 34 12
mov BYTE PTR [rsp+rcx*4+0x12345678],cl         | 88 8c 8c 78 56 34 12
mov BYTE PTR [rsp+rcx*4+0x12345678],dil        | 40 88 bc 8c 78 56 34 12
mov BYTE PTR [rsp+rcx*4+0x12345678],r9b        | 44 88 8c 8c 78 56 34 12
mov DWORD PTR [r11+r8*4+0x12345678],0xdeadbeef | 43 c7 84 83 78 56 34 12 ef be ad de
mov DWORD PTR [rbp+0x7eadbeef],0x12345678      | c7 85 ef be ad 7e 78 56 34 12
mov QWORD PTR [rbp+0x7eadbeef],0x12345678      | 48 c7 85 ef be ad 7e 78 56 34 12
mov QWORD PTR [rbp+r9*4+0x12345678],rsi        | 4a 89 b4 8d 78 56 34 12
mov WORD PTR [r11+r8*4+0x12345678],0xbeef      | 66 43 c7 84 83 78 56 34 12 ef be
mov al,BYTE PTR [rax+rbx*8+0x12345678]         | 8a 84 d8 78 56 34 12
mov esi,0x12345678                             | be 78 56 34 12
mov r11b,0x12                                  | 41 b3 12
mov r8w,0x1234                                 | 66 41 b8 34 12
mov r9,0x12345678                              | 49 c7 c1 78 56 34 12
mov rsi,QWORD PTR [rbp+r9*4+0x12345678]        | 4a 8b b4 8d 78 56 34 12

# Movzx
movzx esi,bl                              | 0f b6 f3
movzx r9d,BYTE PTR [rcx]                  | 44 0f b6 09
movzx r9d,BYTE PTR [rdx+rax*4+0x12345678] | 44 0f b6 8c 82 78 56 34 12
movzx r9d,WORD PTR [rcx]                  | 44 0f b7 09
movzx r9d,WORD PTR [rdx+rax*4+0x12345678] | 44 0f b7 8c 82 78 56 34 12
movzx rsi,bl                              | 48 0f b6 f3
movzx rsi,di                              | 48 0f b7 f7

# Movsx
movsx edi,BYTE PTR [rax+rbx*4+0x12345678] | 0f be bc 98 78 56 34 12
movsx edi,WORD PTR [rax+rbx*4+0x12345678] | 0f bf bc 98 78 56 34 12
movsx esi,bl                              | 0f be f3
movsx rdi,BYTE PTR [rax+rbx*4+0x12345678] | 48 0f be bc 98 78 56 34 12
movsx rdi,WORD PTR [rax+rbx*4+0x12345678] | 48 0f bf bc 98 78 56 34 12
movsx rsi,bl                              | 48 0f be f3
movsx rsi,di                              | 48 0f bf f7

# Movsxd
movsxd r10,eax  | 4c 63 d0
movsxd r10,ebp  | 4c 63 d5
movsxd r10,ebx  | 4c 63 d3
movsxd r10,ecx  | 4c 63 d1
movsxd r10,edi  | 4c 63 d7
movsxd r10,edx  | 4c 63 d2
movsxd r10,esi  | 4c 63 d6
movsxd r10,esp  | 4c 63 d4
movsxd r10,r10d | 4d 63 d2
movsxd r10,r11d | 4d 63 d3
movsxd r10,r12d | 4d 63 d4
movsxd r10,r13d | 4d 63 d5
movsxd r10,r14d | 4d 63 d6
movsxd r10,r15d | 4d 63 d7
movsxd r10,r8d  | 4d 63 d0
movsxd r10,r9d  | 4d 63 d1
movsxd r11,eax  | 4c 63 d8
movsxd r11,ebp  | 4c 63 dd
movsxd r11,ebx  | 4c 63 db
movsxd r11,ecx  | 4c 63 d9
movsxd r11,edi  | 4c 63 df
movsxd r11,edx  | 4c 63 da
movsxd r11,esi  | 4c 63 de
movsxd r11,esp  | 4c 63 dc
movsxd r11,r10d | 4d 63 da
movsxd r11,r11d | 4d 63 db
movsxd r11,r12d | 4d 63 dc
movsxd r11,r13d | 4d 63 dd
movsxd r11,r14d | 4d 63 de
movsxd r11,r15d | 4d 63 df
movsxd r11,r8d  | 4d 63 d8
movsxd r11,r9d  | 4d 63 d9
movsxd r12,eax  | 4c 63 e0
movsxd r12,ebp  | 4c 63 e5
movsxd r12,ebx  | 4c 63 e3
movsxd r12,ecx  | 4c 63 e1
movsxd r12,edi  | 4c 63 e7
movsxd r12,edx  | 4c 63 e2
movsxd r12,esi  | 4c 63 e6
movsxd r12,esp  | 4c 63 e4
movsxd r12,r10d | 4d 63 e2
movsxd r12,r11d | 4d 63 e3
movsxd r12,r12d | 4d 63 e4
movsxd r12,r13d | 4d 63 e5
movsxd r12,r14d | 4d 63 e6
movsxd r12,r15d | 4d 63 e7
movsxd r12,r8d  | 4d 63 e0
movsxd r12,r9d  | 4d 63 e1
movsxd r13,eax  | 4c 63 e8
movsxd r13,ebp  | 4c 63 ed
movsxd r13,ebx  | 4c 63 eb
movsxd r13,ecx  | 4c 63 e9
movsxd r13,edi  | 4c 63 ef
movsxd r13,edx  | 4c 63 ea
movsxd r13,esi  | 4c 63 ee
movsxd r13,esp  | 4c 63 ec
movsxd r13,r10d | 4d 63 ea
movsxd r13,r11d | 4d 63 eb
movsxd r13,r12d | 4d 63 ec
movsxd r13,r13d | 4d 63 ed
movsxd r13,r14d | 4d 63 ee
movsxd r13,r15d | 4d 63 ef
movsxd r13,r8d  | 4d 63 e8
movsxd r13,r9d  | 4d 63 e9
movsxd r14,eax  | 4c 63 f0
movsxd r14,ebp  | 4c 63 f5
movsxd r14,ebx  | 4c 63 f3
movsxd r14,ecx  | 4c 63 f1
movsxd r14,edi  | 4c 63 f7
movsxd r14,edx  | 4c 63 f2
movsxd r14,esi  | 4c 63 f6
movsxd r14,esp  | 4c 63 f4
movsxd r14,r10d | 4d 63 f2
movsxd r14,r11d | 4d 63 f3
movsxd r14,r12d | 4d 63 f4
movsxd r14,r13d | 4d 63 f5
movsxd r14,r14d | 4d 63 f6
movsxd r14,r15d | 4d 63 f7
movsxd r14,r8d  | 4d 63 f0
movsxd r14,r9d  | 4d 63 f1
movsxd r15,eax  | 4c 63 f8
movsxd r15,ebp  | 4c 63 fd
movsxd r15,ebx  | 4c 63 fb
movsxd r15,ecx  | 4c 63 f9
movsxd r15,edi  | 4c 63 ff
movsxd r15,edx  | 4c 63 fa
movsxd r15,esi  | 4c 63 fe
movsxd r15,esp  | 4c 63 fc
movsxd r15,r10d | 4d 63 fa
movsxd r15,r11d | 4d 63 fb
movsxd r15,r12d | 4d 63 fc
movsxd r15,r13d | 4d 63 fd
movsxd r15,r14d | 4d 63 fe
movsxd r15,r15d | 4d 63 ff
movsxd r15,r8d  | 4d 63 f8
movsxd r15,r9d  | 4d 63 f9
movsxd r8,eax   | 4c 63 c0
movsxd r8,ebp   | 4c 63 c5
movsxd r8,ebx   | 4c 63 c3
movsxd r8,ecx   | 4c 63 c1
movsxd r8,edi   | 4c 63 c7
movsxd r8,edx   | 4c 63 c2
movsxd r8,esi   | 4c 63 c6
movsxd r8,esp   | 4c 63 c4
movsxd r8,r10d  | 4d 63 c2
movsxd r8,r11d  | 4d 63 c3
movsxd r8,r12d  | 4d 63 c4
movsxd r8,r13d  | 4d 63 c5
movsxd r8,r14d  | 4d 63 c6
movsxd r8,r15d  | 4d 63 c7
movsxd r8,r8d   | 4d 63 c0
movsxd r8,r9d   | 4d 63 c1
movsxd r9,eax   | 4c 63 c8
movsxd r9,ebp   | 4c 63 cd
movsxd r9,ebx   | 4c 63 cb
movsxd r9,ecx   | 4c 63 c9
movsxd r9,edi   | 4c 63 cf
movsxd r9,edx   | 4c 63 ca
movsxd r9,esi   | 4c 63 ce
movsxd r9,esp   | 4c 63 cc
movsxd r9,r10d  | 4d 63 ca
movsxd r9,r11d  | 4d 63 cb
movsxd r9,r12d  | 4d 63 cc
movsxd r9,r13d  | 4d 63 cd
movsxd r9,r14d  | 4d 63 ce
movsxd r9,r15d  | 4d 63 cf
movsxd r9,r8d   | 4d 63 c8
movsxd r9,r9d   | 4d 63 c9
movsxd rax,eax  | 48 63 c0
movsxd rax,ebp  | 48 63 c5
movsxd rax,ebx  | 48 63 c3
movsxd rax,ecx  | 48 63 c1
movsxd rax,edi  | 48 63 c7
movsxd rax,edx  | 48 63 c2
movsxd rax,esi  | 48 63 c6
movsxd rax,esp  | 48 63 c4
movsxd rax,r10d | 49 63 c2
movsxd rax,r11d | 49 63 c3
movsxd rax,r12d | 49 63 c4
movsxd rax,r13d | 49 63 c5
movsxd rax,r14d | 49 63 c6
movsxd rax,r15d | 49 63 c7
movsxd rax,r8d  | 49 63 c0
movsxd rax,r9d  | 49 63 c1
movsxd rbp,eax  | 48 63 e8
movsxd rbp,ebp  | 48 63 ed
movsxd rbp,ebx  | 48 63 eb
movsxd rbp,ecx  | 48 63 e9
movsxd rbp,edi  | 48 63 ef
movsxd rbp,edx  | 48 63 ea
movsxd rbp,esi  | 48 63 ee
movsxd rbp,esp  | 48 63 ec
movsxd rbp,r10d | 49 63 ea
movsxd rbp,r11d | 49 63 eb
movsxd rbp,r12d | 49 63 ec
movsxd rbp,r13d | 49 63 ed
movsxd rbp,r14d | 49 63 ee
movsxd rbp,r15d | 49 63 ef
movsxd rbp,r8d  | 49 63 e8
movsxd rbp,r9d  | 49 63 e9
movsxd rbx,eax  | 48 63 d8
movsxd rbx,ebp  | 48 63 dd
movsxd rbx,ebx  | 48 63 db
movsxd rbx,ecx  | 48 63 d9
movsxd rbx,edi  | 48 63 df
movsxd rbx,edx  | 48 63 da
movsxd rbx,esi  | 48 63 de
movsxd rbx,esp  | 48 63 dc
movsxd rbx,r10d | 49 63 da
movsxd rbx,r11d | 49 63 db
movsxd rbx,r12d | 49 63 dc
movsxd rbx,r13d | 49 63 dd
movsxd rbx,r14d | 49 63 de
movsxd rbx,r15d | 49 63 df
movsxd rbx,r8d  | 49 63 d8
movsxd rbx,r9d  | 49 63 d9
movsxd rcx,eax  | 48 63 c8
movsxd rcx,ebp  | 48 63 cd
movsxd rcx,ebx  | 48 63 cb
movsxd rcx,ecx  | 48 63 c9
movsxd rcx,edi  | 48 63 cf
movsxd rcx,edx  | 48 63 ca
movsxd rcx,esi  | 48 63 ce
movsxd rcx,esp  | 48 63 cc
movsxd rcx,r10d | 49 63 ca
movsxd rcx,r11d | 49 63 cb
movsxd rcx,r12d | 49 63 cc
movsxd rcx,r13d | 49 63 cd
movsxd rcx,r14d | 49 63 ce
movsxd rcx,r15d | 49 63 cf
movsxd rcx,r8d  | 49 63 c8
movsxd rcx,r9d  | 49 63 c9
movsxd rdi,eax  | 48 63 f8
movsxd rdi,ebp  | 48 63 fd
movsxd rdi,ebx  | 48 63 fb
movsxd rdi,ecx  | 48 63 f9
movsxd rdi,edi  | 48 63 ff
movsxd rdi,edx  | 48 63 fa
movsxd rdi,esi  | 48 63 fe
movsxd rdi,esp  | 48 63 fc
movsxd rdi,r10d | 49 63 fa
movsxd rdi,r11d | 49 63 fb
movsxd rdi,r12d | 49 63 fc
movsxd rdi,r13d | 49 63 fd
movsxd rdi,r14d | 49 63 fe
movsxd rdi,r15d | 49 63 ff
movsxd rdi,r8d  | 49 63 f8
movsxd rdi,r9d  | 49 63 f9
movsxd rdx,eax  | 48 63 d0
movsxd rdx,ebp  | 48 63 d5
movsxd rdx,ebx  | 48 63 d3
movsxd rdx,ecx  | 48 63 d1
movsxd rdx,edi  | 48 63 d7
movsxd rdx,edx  | 48 63 d2
movsxd rdx,esi  | 48 63 d6
movsxd rdx,esp  | 48 63 d4
movsxd rdx,r10d | 49 63 d2
movsxd rdx,r11d | 49 63 d3
movsxd rdx,r12d | 49 63 d4
movsxd rdx,r13d | 49 63 d5
movsxd rdx,r14d | 49 63 d6
movsxd rdx,r15d | 49 63 d7
movsxd rdx,r8d  | 49 63 d0
movsxd rdx,r9d  | 49 63 d1
movsxd rsi,eax  | 48 63 f0
movsxd rsi,ebp  | 48 63 f5
movsxd rsi,ebx  | 48 63 f3
movsxd rsi,ecx  | 48 63 f1
movsxd rsi,edi  | 48 63 f7
movsxd rsi,edx  | 48 63 f2
movsxd rsi,esi  | 48 63 f6
movsxd rsi,esp  | 48 63 f4
movsxd rsi,r10d | 49 63 f2
movsxd rsi,r11d | 49 63 f3
movsxd rsi,r12d | 49 63 f4
movsxd rsi,r13d | 49 63 f5
movsxd rsi,r14d | 49 63 f6
movsxd rsi,r15d | 49 63 f7
movsxd rsi,r8d  | 49 63 f0
movsxd rsi,r9d  | 49 63 f1
movsxd rsp,eax  | 48 63 e0
movsxd rsp,ebp  | 48 63 e5
movsxd rsp,ebx  | 48 63 e3
movsxd rsp,ecx  | 48 63 e1
movsxd rsp,edi  | 48 63 e7
movsxd rsp,edx  | 48 63 e2
movsxd rsp,esi  | 48 63 e6
movsxd rsp,esp  | 48 63 e4
movsxd rsp,r10d | 49 63 e2
movsxd rsp,r11d | 49 63 e3
movsxd rsp,r12d | 49 63 e4
movsxd rsp,r13d | 49 63 e5
movsxd rsp,r14d | 49 63 e6
movsxd rsp,r15d | 49 63 e7
movsxd rsp,r8d  | 49 63 e0
movsxd rsp,r9d  | 49 63 e1
#
movsxd r10,DWORD PTR [r10] | 4d 63 12
movsxd r10,DWORD PTR [r11] | 4d 63 13
movsxd r10,DWORD PTR [r12] | 4d 63 14 24
movsxd r10,DWORD PTR [r13] | 4d 63 55 00
movsxd r10,DWORD PTR [r14] | 4d 63 16
movsxd r10,DWORD PTR [r15] | 4d 63 17
movsxd r10,DWORD PTR [r8]  | 4d 63 10
movsxd r10,DWORD PTR [r9]  | 4d 63 11
movsxd r10,DWORD PTR [rax] | 4c 63 10
movsxd r10,DWORD PTR [rbp] | 4c 63 55 00
movsxd r10,DWORD PTR [rbx] | 4c 63 13
movsxd r10,DWORD PTR [rcx] | 4c 63 11
movsxd r10,DWORD PTR [rdi] | 4c 63 17
movsxd r10,DWORD PTR [rdx] | 4c 63 12
movsxd r10,DWORD PTR [rsi] | 4c 63 16
movsxd r10,DWORD PTR [rsp] | 4c 63 14 24
movsxd r11,DWORD PTR [r10] | 4d 63 1a
movsxd r11,DWORD PTR [r11] | 4d 63 1b
movsxd r11,DWORD PTR [r12] | 4d 63 1c 24
movsxd r11,DWORD PTR [r13] | 4d 63 5d 00
movsxd r11,DWORD PTR [r14] | 4d 63 1e
movsxd r11,DWORD PTR [r15] | 4d 63 1f
movsxd r11,DWORD PTR [r8]  | 4d 63 18
movsxd r11,DWORD PTR [r9]  | 4d 63 19
movsxd r11,DWORD PTR [rax] | 4c 63 18
movsxd r11,DWORD PTR [rbp] | 4c 63 5d 00
movsxd r11,DWORD PTR [rbx] | 4c 63 1b
movsxd r11,DWORD PTR [rcx] | 4c 63 19
movsxd r11,DWORD PTR [rdi] | 4c 63 1f
movsxd r11,DWORD PTR [rdx] | 4c 63 1a
movsxd r11,DWORD PTR [rsi] | 4c 63 1e
movsxd r11,DWORD PTR [rsp] | 4c 63 1c 24
movsxd r12,DWORD PTR [r10] | 4d 63 22
movsxd r12,DWORD PTR [r11] | 4d 63 23
movsxd r12,DWORD PTR [r12] | 4d 63 24 24
movsxd r12,DWORD PTR [r13] | 4d 63 65 00
movsxd r12,DWORD PTR [r14] | 4d 63 26
movsxd r12,DWORD PTR [r15] | 4d 63 27
movsxd r12,DWORD PTR [r8]  | 4d 63 20
movsxd r12,DWORD PTR [r9]  | 4d 63 21
movsxd r12,DWORD PTR [rax] | 4c 63 20
movsxd r12,DWORD PTR [rbp] | 4c 63 65 00
movsxd r12,DWORD PTR [rbx] | 4c 63 23
movsxd r12,DWORD PTR [rcx] | 4c 63 21
movsxd r12,DWORD PTR [rdi] | 4c 63 27
movsxd r12,DWORD PTR [rdx] | 4c 63 22
movsxd r12,DWORD PTR [rsi] | 4c 63 26
movsxd r12,DWORD PTR [rsp] | 4c 63 24 24
movsxd r13,DWORD PTR [r10] | 4d 63 2a
movsxd r13,DWORD PTR [r11] | 4d 63 2b
movsxd r13,DWORD PTR [r12] | 4d 63 2c 24
movsxd r13,DWORD PTR [r13] | 4d 63 6d 00
movsxd r13,DWORD PTR [r14] | 4d 63 2e
movsxd r13,DWORD PTR [r15] | 4d 63 2f
movsxd r13,DWORD PTR [r8]  | 4d 63 28
movsxd r13,DWORD PTR [r9]  | 4d 63 29
movsxd r13,DWORD PTR [rax] | 4c 63 28
movsxd r13,DWORD PTR [rbp] | 4c 63 6d 00
movsxd r13,DWORD PTR [rbx] | 4c 63 2b
movsxd r13,DWORD PTR [rcx] | 4c 63 29
movsxd r13,DWORD PTR [rdi] | 4c 63 2f
movsxd r13,DWORD PTR [rdx] | 4c 63 2a
movsxd r13,DWORD PTR [rsi] | 4c 63 2e
movsxd r13,DWORD PTR [rsp] | 4c 63 2c 24
movsxd r14,DWORD PTR [r10] | 4d 63 32
movsxd r14,DWORD PTR [r11] | 4d 63 33
movsxd r14,DWORD PTR [r12] | 4d 63 34 24
movsxd r14,DWORD PTR [r13] | 4d 63 75 00
movsxd r14,DWORD PTR [r14] | 4d 63 36
movsxd r14,DWORD PTR [r15] | 4d 63 37
movsxd r14,DWORD PTR [r8]  | 4d 63 30
movsxd r14,DWORD PTR [r9]  | 4d 63 31
movsxd r14,DWORD PTR [rax] | 4c 63 30
movsxd r14,DWORD PTR [rbp] | 4c 63 75 00
movsxd r14,DWORD PTR [rbx] | 4c 63 33
movsxd r14,DWORD PTR [rcx] | 4c 63 31
movsxd r14,DWORD PTR [rdi] | 4c 63 37
movsxd r14,DWORD PTR [rdx] | 4c 63 32
movsxd r14,DWORD PTR [rsi] | 4c 63 36
movsxd r14,DWORD PTR [rsp] | 4c 63 34 24
movsxd r15,DWORD PTR [r10] | 4d 63 3a
movsxd r15,DWORD PTR [r11] | 4d 63 3b
movsxd r15,DWORD PTR [r12] | 4d 63 3c 24
movsxd r15,DWORD PTR [r13] | 4d 63 7d 00
movsxd r15,DWORD PTR [r14] | 4d 63 3e
movsxd r15,DWORD PTR [r15] | 4d 63 3f
movsxd r15,DWORD PTR [r8]  | 4d 63 38
movsxd r15,DWORD PTR [r9]  | 4d 63 39
movsxd r15,DWORD PTR [rax] | 4c 63 38
movsxd r15,DWORD PTR [rbp] | 4c 63 7d 00
movsxd r15,DWORD PTR [rbx] | 4c 63 3b
movsxd r15,DWORD PTR [rcx] | 4c 63 39
movsxd r15,DWORD PTR [rdi] | 4c 63 3f
movsxd r15,DWORD PTR [rdx] | 4c 63 3a
movsxd r15,DWORD PTR [rsi] | 4c 63 3e
movsxd r15,DWORD PTR [rsp] | 4c 63 3c 24
movsxd r8,DWORD PTR [r10]  | 4d 63 02
movsxd r8,DWORD PTR [r11]  | 4d 63 03
movsxd r8,DWORD PTR [r12]  | 4d 63 04 24
movsxd r8,DWORD PTR [r13]  | 4d 63 45 00
movsxd r8,DWORD PTR [r14]  | 4d 63 06
movsxd r8,DWORD PTR [r15]  | 4d 63 07
movsxd r8,DWORD PTR [r8]   | 4d 63 00
movsxd r8,DWORD PTR [r9]   | 4d 63 01
movsxd r8,DWORD PTR [rax]  | 4c 63 00
movsxd r8,DWORD PTR [rbp]  | 4c 63 45 00
movsxd r8,DWORD PTR [rbx]  | 4c 63 03
movsxd r8,DWORD PTR [rcx]  | 4c 63 01
movsxd r8,DWORD PTR [rdi]  | 4c 63 07
movsxd r8,DWORD PTR [rdx]  | 4c 63 02
movsxd r8,DWORD PTR [rsi]  | 4c 63 06
movsxd r8,DWORD PTR [rsp]  | 4c 63 04 24
movsxd r9,DWORD PTR [r10]  | 4d 63 0a
movsxd r9,DWORD PTR [r11]  | 4d 63 0b
movsxd r9,DWORD PTR [r12]  | 4d 63 0c 24
movsxd r9,DWORD PTR [r13]  | 4d 63 4d 00
movsxd r9,DWORD PTR [r14]  | 4d 63 0e
movsxd r9,DWORD PTR [r15]  | 4d 63 0f
movsxd r9,DWORD PTR [r8]   | 4d 63 08
movsxd r9,DWORD PTR [r9]   | 4d 63 09
movsxd r9,DWORD PTR [rax]  | 4c 63 08
movsxd r9,DWORD PTR [rbp]  | 4c 63 4d 00
movsxd r9,DWORD PTR [rbx]  | 4c 63 0b
movsxd r9,DWORD PTR [rcx]  | 4c 63 09
movsxd r9,DWORD PTR [rdi]  | 4c 63 0f
movsxd r9,DWORD PTR [rdx]  | 4c 63 0a
movsxd r9,DWORD PTR [rsi]  | 4c 63 0e
movsxd r9,DWORD PTR [rsp]  | 4c 63 0c 24
movsxd rax,DWORD PTR [r10] | 49 63 02
movsxd rax,DWORD PTR [r11] | 49 63 03
movsxd rax,DWORD PTR [r12] | 49 63 04 24
movsxd rax,DWORD PTR [r13] | 49 63 45 00
movsxd rax,DWORD PTR [r14] | 49 63 06
movsxd rax,DWORD PTR [r15] | 49 63 07
movsxd rax,DWORD PTR [r8]  | 49 63 00
movsxd rax,DWORD PTR [r9]  | 49 63 01
movsxd rax,DWORD PTR [rax] | 48 63 00
movsxd rax,DWORD PTR [rbp] | 48 63 45 00
movsxd rax,DWORD PTR [rbx] | 48 63 03
movsxd rax,DWORD PTR [rcx] | 48 63 01
movsxd rax,DWORD PTR [rdi] | 48 63 07
movsxd rax,DWORD PTR [rdx] | 48 63 02
movsxd rax,DWORD PTR [rsi] | 48 63 06
movsxd rax,DWORD PTR [rsp] | 48 63 04 24
movsxd rbp,DWORD PTR [r10] | 49 63 2a
movsxd rbp,DWORD PTR [r11] | 49 63 2b
movsxd rbp,DWORD PTR [r12] | 49 63 2c 24
movsxd rbp,DWORD PTR [r13] | 49 63 6d 00
movsxd rbp,DWORD PTR [r14] | 49 63 2e
movsxd rbp,DWORD PTR [r15] | 49 63 2f
movsxd rbp,DWORD PTR [r8]  | 49 63 28
movsxd rbp,DWORD PTR [r9]  | 49 63 29
movsxd rbp,DWORD PTR [rax] | 48 63 28
movsxd rbp,DWORD PTR [rbp] | 48 63 6d 00
movsxd rbp,DWORD PTR [rbx] | 48 63 2b
movsxd rbp,DWORD PTR [rcx] | 48 63 29
movsxd rbp,DWORD PTR [rdi] | 48 63 2f
movsxd rbp,DWORD PTR [rdx] | 48 63 2a
movsxd rbp,DWORD PTR [rsi] | 48 63 2e
movsxd rbp,DWORD PTR [rsp] | 48 63 2c 24
movsxd rbx,DWORD PTR [r10] | 49 63 1a
movsxd rbx,DWORD PTR [r11] | 49 63 1b
movsxd rbx,DWORD PTR [r12] | 49 63 1c 24
movsxd rbx,DWORD PTR [r13] | 49 63 5d 00
movsxd rbx,DWORD PTR [r14] | 49 63 1e
movsxd rbx,DWORD PTR [r15] | 49 63 1f
movsxd rbx,DWORD PTR [r8]  | 49 63 18
movsxd rbx,DWORD PTR [r9]  | 49 63 19
movsxd rbx,DWORD PTR [rax] | 48 63 18
movsxd rbx,DWORD PTR [rbp] | 48 63 5d 00
movsxd rbx,DWORD PTR [rbx] | 48 63 1b
movsxd rbx,DWORD PTR [rcx] | 48 63 19
movsxd rbx,DWORD PTR [rdi] | 48 63 1f
movsxd rbx,DWORD PTR [rdx] | 48 63 1a
movsxd rbx,DWORD PTR [rsi] | 48 63 1e
movsxd rbx,DWORD PTR [rsp] | 48 63 1c 24
movsxd rcx,DWORD PTR [r10] | 49 63 0a
movsxd rcx,DWORD PTR [r11] | 49 63 0b
movsxd rcx,DWORD PTR [r12] | 49 63 0c 24
movsxd rcx,DWORD PTR [r13] | 49 63 4d 00
movsxd rcx,DWORD PTR [r14] | 49 63 0e
movsxd rcx,DWORD PTR [r15] | 49 63 0f
movsxd rcx,DWORD PTR [r8]  | 49 63 08
movsxd rcx,DWORD PTR [r9]  | 49 63 09
movsxd rcx,DWORD PTR [rax] | 48 63 08
movsxd rcx,DWORD PTR [rbp] | 48 63 4d 00
movsxd rcx,DWORD PTR [rbx] | 48 63 0b
movsxd rcx,DWORD PTR [rcx] | 48 63 09
movsxd rcx,DWORD PTR [rdi] | 48 63 0f
movsxd rcx,DWORD PTR [rdx] | 48 63 0a
movsxd rcx,DWORD PTR [rsi] | 48 63 0e
movsxd rcx,DWORD PTR [rsp] | 48 63 0c 24
movsxd rdi,DWORD PTR [r10] | 49 63 3a
movsxd rdi,DWORD PTR [r11] | 49 63 3b
movsxd rdi,DWORD PTR [r12] | 49 63 3c 24
movsxd rdi,DWORD PTR [r13] | 49 63 7d 00
movsxd rdi,DWORD PTR [r14] | 49 63 3e
movsxd rdi,DWORD PTR [r15] | 49 63 3f
movsxd rdi,DWORD PTR [r8]  | 49 63 38
movsxd rdi,DWORD PTR [r9]  | 49 63 39
movsxd rdi,DWORD PTR [rax] | 48 63 38
movsxd rdi,DWORD PTR [rbp] | 48 63 7d 00
movsxd rdi,DWORD PTR [rbx] | 48 63 3b
movsxd rdi,DWORD PTR [rcx] | 48 63 39
movsxd rdi,DWORD PTR [rdi] | 48 63 3f
movsxd rdi,DWORD PTR [rdx] | 48 63 3a
movsxd rdi,DWORD PTR [rsi] | 48 63 3e
movsxd rdi,DWORD PTR [rsp] | 48 63 3c 24
movsxd rdx,DWORD PTR [r10] | 49 63 12
movsxd rdx,DWORD PTR [r11] | 49 63 13
movsxd rdx,DWORD PTR [r12] | 49 63 14 24
movsxd rdx,DWORD PTR [r13] | 49 63 55 00
movsxd rdx,DWORD PTR [r14] | 49 63 16
movsxd rdx,DWORD PTR [r15] | 49 63 17
movsxd rdx,DWORD PTR [r8]  | 49 63 10
movsxd rdx,DWORD PTR [r9]  | 49 63 11
movsxd rdx,DWORD PTR [rax] | 48 63 10
movsxd rdx,DWORD PTR [rbp] | 48 63 55 00
movsxd rdx,DWORD PTR [rbx] | 48 63 13
movsxd rdx,DWORD PTR [rcx] | 48 63 11
movsxd rdx,DWORD PTR [rdi] | 48 63 17
movsxd rdx,DWORD PTR [rdx] | 48 63 12
movsxd rdx,DWORD PTR [rsi] | 48 63 16
movsxd rdx,DWORD PTR [rsp] | 48 63 14 24
movsxd rsi,DWORD PTR [r10] | 49 63 32
movsxd rsi,DWORD PTR [r11] | 49 63 33
movsxd rsi,DWORD PTR [r12] | 49 63 34 24
movsxd rsi,DWORD PTR [r13] | 49 63 75 00
movsxd rsi,DWORD PTR [r14] | 49 63 36
movsxd rsi,DWORD PTR [r15] | 49 63 37
movsxd rsi,DWORD PTR [r8]  | 49 63 30
movsxd rsi,DWORD PTR [r9]  | 49 63 31
movsxd rsi,DWORD PTR [rax] | 48 63 30
movsxd rsi,DWORD PTR [rbp] | 48 63 75 00
movsxd rsi,DWORD PTR [rbx] | 48 63 33
movsxd rsi,DWORD PTR [rcx] | 48 63 31
movsxd rsi,DWORD PTR [rdi] | 48 63 37
movsxd rsi,DWORD PTR [rdx] | 48 63 32
movsxd rsi,DWORD PTR [rsi] | 48 63 36
movsxd rsi,DWORD PTR [rsp] | 48 63 34 24
movsxd rsp,DWORD PTR [r10] | 49 63 22
movsxd rsp,DWORD PTR [r11] | 49 63 23
movsxd rsp,DWORD PTR [r12] | 49 63 24 24
movsxd rsp,DWORD PTR [r13] | 49 63 65 00
movsxd rsp,DWORD PTR [r14] | 49 63 26
movsxd rsp,DWORD PTR [r15] | 49 63 27
movsxd rsp,DWORD PTR [r8]  | 49 63 20
movsxd rsp,DWORD PTR [r9]  | 49 63 21
movsxd rsp,DWORD PTR [rax] | 48 63 20
movsxd rsp,DWORD PTR [rbp] | 48 63 65 00
movsxd rsp,DWORD PTR [rbx] | 48 63 23
movsxd rsp,DWORD PTR [rcx] | 48 63 21
movsxd rsp,DWORD PTR [rdi] | 48 63 27
movsxd rsp,DWORD PTR [rdx] | 48 63 22
movsxd rsp,DWORD PTR [rsi] | 48 63 26
movsxd rsp,DWORD PTR [rsp] | 48 63 24 24
#
movsxd rdx,DWORD PTR [r11+r15*4+0x12345678] | 4b 63 94 bb 78 56 34 12

# Push
push 0x12345678 | 68 78 56 34 12
push ax         | 66 50
push bp         | 66 55
push bx         | 66 53
push cx         | 66 51
push di         | 66 57
push dx         | 66 52
push r10        | 41 52
push r10w       | 66 41 52
push r11        | 41 53
push r11w       | 66 41 53
push r12        | 41 54
push r12w       | 66 41 54
push r13        | 41 55
push r13w       | 66 41 55
push r14        | 41 56
push r14w       | 66 41 56
push r15        | 41 57
push r15w       | 66 41 57
push r8         | 41 50
push r8w        | 66 41 50
push r9         | 41 51
push r9w        | 66 41 51
push rax        | 50
push rbp        | 55
push rbx        | 53
push rcx        | 51
push rdi        | 57
push rdx        | 52
push rsi        | 56
push rsp        | 54
push si         | 66 56
push sp         | 66 54
#
push 0x12                              | 6a 12
push QWORD PTR [edx]                   | 67 ff 32
push QWORD PTR [r11+rsi*8+0x12345678]  | 41 ff b4 f3 78 56 34 12
push QWORD PTR [r11d+edi*8+0x12345678] | 67 41 ff b4 fb 78 56 34 12
push QWORD PTR [rdx]                   | ff 32

# Pop
pop ax   | 66 58
pop bp   | 66 5d
pop bx   | 66 5b
pop cx   | 66 59
pop di   | 66 5f
pop dx   | 66 5a
pop r10  | 41 5a
pop r10w | 66 41 5a
pop r11  | 41 5b
pop r11w | 66 41 5b
pop r12  | 41 5c
pop r12w | 66 41 5c
pop r13  | 41 5d
pop r13w | 66 41 5d
pop r14  | 41 5e
pop r14w | 66 41 5e
pop r15  | 41 5f
pop r15w | 66 41 5f
pop r8   | 41 58
pop r8w  | 66 41 58
pop r9   | 41 59
pop r9w  | 66 41 59
pop rax  | 58
pop rbp  | 5d
pop rbx  | 5b
pop rcx  | 59
pop rdi  | 5f
pop rdx  | 5a
pop rsi  | 5e
pop rsp  | 5c
pop si   | 66 5e
pop sp   | 66 5c

# Leave
leave | c9

# Ret
ret | c3

# Cpuid
cpuid | 0f a2

# Hlt
hlt | f4

# Add
add DWORD PTR [eax+ebx*4+0x12345678],r8d                | 67 44 01 84 98 78 56 34 12
add DWORD PTR [rax+rbx*4+0x12345678],r8d                | 44 01 84 98 78 56 34 12
add QWORD PTR [rax+rbx*4+0x12345678],r9                 | 4c 01 8c 98 78 56 34 12
add QWORD PTR [rax+rbx*4+0x12345678],rsp                | 48 01 a4 98 78 56 34 12
add QWORD PTR [rsp+rbp*4+0x7eadbeef],0x0000000000000012 | 48 83 84 ac ef be ad 7e 12
add WORD PTR [rax+rbx*4+0x12345678],r8w                 | 66 44 01 84 98 78 56 34 12
add al,0x99                                             | 04 99
add ax,0x1234                                           | 66 05 34 12
add cx,0x1234                                           | 66 81 c1 34 12
add eax,0x00000018                                      | 83 c0 18
add eax,0x12345678                                      | 05 78 56 34 12
add esp,DWORD PTR [rax+rbx*4+0x12345678]                | 03 a4 98 78 56 34 12
add r11d,DWORD PTR [rax+rbx*4+0x12345678]               | 44 03 9c 98 78 56 34 12
add r8,0x0000000000000001                               | 49 83 c0 01
add r8,r9                                               | 4d 01 c8
add r9,0x0000000000000012                               | 49 83 c1 12
add r9,QWORD PTR [rax+rbx*4+0x12345678]                 | 4c 03 8c 98 78 56 34 12
add rax,0x0000000000000001                              | 48 83 c0 01
add rax,0x0000000000000012                              | 48 83 c0 12
add rax,0x0000000012345678                              | 48 05 78 56 34 12
add rsp,0x0000000012345678                              | 48 81 c4 78 56 34 12
add rsp,QWORD PTR [rax+rbx*4+0x12345678]                | 48 03 a4 98 78 56 34 12

# Adc
adc cx,0x1234 | 66 81 d1 34 12

# And
and al,0x12                              | 24 12
and al,BYTE PTR [rax+rbx*4+0x12345678]   | 22 84 98 78 56 34 12
and ax,WORD PTR [rax+rbx*4+0x12345678]   | 66 23 84 98 78 56 34 12
and cx,0x1234                            | 66 81 e1 34 12
and di,0x00f0                            | 66 81 e7 f0 00
and di,0xfff0                            | 66 83 e7 f0
and dx,WORD PTR [r10]                    | 66 41 23 12
and eax,0x00000012                       | 83 e0 12
and eax,0x12345678                       | 25 78 56 34 12
and eax,DWORD PTR [rax+rbx*4+0x12345678] | 23 84 98 78 56 34 12
and ecx,DWORD PTR [r10]                  | 41 23 0a
and edi,0x00000012                       | 83 e7 12
and edi,0x12345678                       | 81 e7 78 56 34 12
and edi,0xfffffff0                       | 83 e7 f0
and r12,r13                              | 4d 21 ec
and r15d,0x0000001f                      | 41 83 e7 1f
and rax,0x0000000000000012               | 48 83 e0 12
and rax,0x0000000012345678               | 48 25 78 56 34 12
and rax,QWORD PTR [rax+rbx*4+0x12345678] | 48 23 84 98 78 56 34 12
and rcx,QWORD PTR [r10]                  | 49 23 0a
and rdi,0x0000000000000012               | 48 83 e7 12
and rdi,0x0000000012345678               | 48 81 e7 78 56 34 12
and rdi,0xfffffffffedcba98               | 48 81 e7 98 ba dc fe
and rdi,0xfffffffffffffff0               | 48 83 e7 f0
and rdi,0xfffffffffffffffe               | 48 83 e7 fe
and spl,BYTE PTR [r10]                   | 41 22 22

# Sub
sub DWORD PTR [eax+ebx*4+0x12345678],r8d  | 67 44 29 84 98 78 56 34 12
sub DWORD PTR [rax+rbx*4+0x12345678],r8d  | 44 29 84 98 78 56 34 12
sub QWORD PTR [rax+rbx*4+0x12345678],r9   | 4c 29 8c 98 78 56 34 12
sub QWORD PTR [rax+rbx*4+0x12345678],rsp  | 48 29 a4 98 78 56 34 12
sub WORD PTR [rax+rbx*4+0x12345678],r8w   | 66 44 29 84 98 78 56 34 12
sub cx,0x1234                             | 66 81 e9 34 12
sub esi,0x00000012                        | 83 ee 12
sub esp,DWORD PTR [rax+rbx*4+0x12345678]  | 2b a4 98 78 56 34 12
sub r11d,DWORD PTR [rax+rbx*4+0x12345678] | 44 2b 9c 98 78 56 34 12
sub r8,r9                                 | 4d 29 c8
sub r9,QWORD PTR [rax+rbx*4+0x12345678]   | 4c 2b 8c 98 78 56 34 12
sub rdi,0x0000000000000012                | 48 83 ef 12
sub rsp,0x0000000012345678                | 48 81 ec 78 56 34 12
sub rsp,QWORD PTR [rax+rbx*4+0x12345678]  | 48 2b a4 98 78 56 34 12

# Sbb
sbb al,0x12   | 1c 12
sbb ax,0x1234 | 66 1d 34 12
sbb cx,0x1234 | 66 81 d9 34 12
sbb esi,esi   | 19 f6
sbb r12d,r12d | 45 19 e4
sbb rax,rax   | 48 19 c0

# Shr
shr bpl,0x01 | 40 d0 ed
shr bx,0x12  | 66 c1 eb 12
shr di,0x01  | 66 d1 ef
shr eax,cl   | d3 e8
shr ecx,0x12 | c1 e9 12
shr edx,0x01 | d1 ea
shr r11b,cl  | 41 d2 eb
shr r9,0x01  | 49 d1 e9
shr rcx,cl   | 48 d3 e9
shr rdx,0x12 | 48 c1 ea 12
shr si,cl    | 66 d3 ee
shr sil,0x01 | 40 d0 ee
shr spl,0x01 | 40 d0 ec

# Sar
sar bpl,0x01 | 40 d0 fd
sar bx,0x12  | 66 c1 fb 12
sar di,0x01  | 66 d1 ff
sar eax,cl   | d3 f8
sar ecx,0x12 | c1 f9 12
sar edx,0x01 | d1 fa
sar r11b,cl  | 41 d2 fb
sar r9,0x01  | 49 d1 f9
sar rcx,cl   | 48 d3 f9
sar rdx,0x12 | 48 c1 fa 12
sar si,cl    | 66 d3 fe
sar sil,0x01 | 40 d0 fe
sar spl,0x01 | 40 d0 fc

# Shl
shl bpl,0x01 | 40 d0 e5
shl bx,0x12  | 66 c1 e3 12
shl di,0x01  | 66 d1 e7
shl eax,cl   | d3 e0
shl ecx,0x12 | c1 e1 12
shl edx,0x01 | d1 e2
shl r11b,cl  | 41 d2 e3
shl r9,0x01  | 49 d1 e1
shl rcx,cl   | 48 d3 e1
shl rdx,0x12 | 48 c1 e2 12
shl si,cl    | 66 d3 e6
shl sil,0x01 | 40 d0 e6
shl spl,0x01 | 40 d0 e4

# Imul
imul eax,ebx,0x12                                   | 6b c3 12
imul edi,DWORD PTR [rax+r12*8+0x12345678]           | 42 0f af bc e0 78 56 34 12
imul r9,QWORD PTR [r11+r12*4+0x12345678],0x7eadbeef | 4f 69 8c a3 78 56 34 12 ef be ad 7e
imul r9,QWORD PTR [rax],0x7eadbeef                  | 4c 69 08 ef be ad 7e
imul rbx,rbp                                        | 48 0f af dd
imul rdx,r9,0x58                                    | 49 6b d1 58

# Idiv
idiv eax | f7 f8
idiv esi | f7 fe
idiv r11 | 49 f7 fb
idiv r9d | 41 f7 f9
idiv rax | 48 f7 f8
idiv rsi | 48 f7 fe

# Div
div ah   | f6 f4
div al   | f6 f0
div ax   | 66 f7 f0
div bh   | f6 f7
div bl   | f6 f3
div bp   | 66 f7 f5
div bpl  | 40 f6 f5
div bx   | 66 f7 f3
div ch   | f6 f5
div cl   | f6 f1
div cx   | 66 f7 f1
div dh   | f6 f6
div di   | 66 f7 f7
div dil  | 40 f6 f7
div dl   | f6 f2
div dx   | 66 f7 f2
div eax  | f7 f0
div ebp  | f7 f5
div ebx  | f7 f3
div ecx  | f7 f1
div edi  | f7 f7
div edx  | f7 f2
div esi  | f7 f6
div esp  | f7 f4
div r10  | 49 f7 f2
div r10b | 41 f6 f2
div r10d | 41 f7 f2
div r10w | 66 41 f7 f2
div r11  | 49 f7 f3
div r11b | 41 f6 f3
div r11d | 41 f7 f3
div r11w | 66 41 f7 f3
div r12  | 49 f7 f4
div r12b | 41 f6 f4
div r12d | 41 f7 f4
div r12w | 66 41 f7 f4
div r13  | 49 f7 f5
div r13b | 41 f6 f5
div r13d | 41 f7 f5
div r13w | 66 41 f7 f5
div r14  | 49 f7 f6
div r14b | 41 f6 f6
div r14d | 41 f7 f6
div r14w | 66 41 f7 f6
div r15  | 49 f7 f7
div r15b | 41 f6 f7
div r15d | 41 f7 f7
div r15w | 66 41 f7 f7
div r8   | 49 f7 f0
div r8b  | 41 f6 f0
div r8d  | 41 f7 f0
div r8w  | 66 41 f7 f0
div r9   | 49 f7 f1
div r9b  | 41 f6 f1
div r9d  | 41 f7 f1
div r9w  | 66 41 f7 f1
div rax  | 48 f7 f0
div rbp  | 48 f7 f5
div rbx  | 48 f7 f3
div rcx  | 48 f7 f1
div rdi  | 48 f7 f7
div rdx  | 48 f7 f2
div rsi  | 48 f7 f6
div rsp  | 48 f7 f4
div si   | 66 f7 f6
div sil  | 40 f6 f6
div sp   | 66 f7 f4
div spl  | 40 f6 f4
#
div BYTE PTR [rax]                   | f6 30
div BYTE PTR [rbx+r11*8+0x12345678]  | 42 f6 b4 db 78 56 34 12
div DWORD PTR [rax]                  | f7 30
div DWORD PTR [rbx+r11*8+0x12345678] | 42 f7 b4 db 78 56 34 12
div QWORD PTR [rax]                  | 48 f7 30
div QWORD PTR [rbx+r11*8+0x12345678] | 4a f7 b4 db 78 56 34 12
div WORD PTR [rax]                   | 66 f7 30
div WORD PTR [rbx+r11*8+0x12345678]  | 66 42 f7 b4 db 78 56 34 12

# Mul
mul ah   | f6 e4
mul al   | f6 e0
mul ax   | 66 f7 e0
mul bh   | f6 e7
mul bl   | f6 e3
mul bp   | 66 f7 e5
mul bpl  | 40 f6 e5
mul bx   | 66 f7 e3
mul ch   | f6 e5
mul cl   | f6 e1
mul cx   | 66 f7 e1
mul dh   | f6 e6
mul di   | 66 f7 e7
mul dil  | 40 f6 e7
mul dl   | f6 e2
mul dx   | 66 f7 e2
mul eax  | f7 e0
mul ebp  | f7 e5
mul ebx  | f7 e3
mul ecx  | f7 e1
mul edi  | f7 e7
mul edx  | f7 e2
mul esi  | f7 e6
mul esp  | f7 e4
mul r10  | 49 f7 e2
mul r10b | 41 f6 e2
mul r10d | 41 f7 e2
mul r10w | 66 41 f7 e2
mul r11  | 49 f7 e3
mul r11b | 41 f6 e3
mul r11d | 41 f7 e3
mul r11w | 66 41 f7 e3
mul r12  | 49 f7 e4
mul r12b | 41 f6 e4
mul r12d | 41 f7 e4
mul r12w | 66 41 f7 e4
mul r13  | 49 f7 e5
mul r13b | 41 f6 e5
mul r13d | 41 f7 e5
mul r13w | 66 41 f7 e5
mul r14  | 49 f7 e6
mul r14b | 41 f6 e6
mul r14d | 41 f7 e6
mul r14w | 66 41 f7 e6
mul r15  | 49 f7 e7
mul r15b | 41 f6 e7
mul r15d | 41 f7 e7
mul r15w | 66 41 f7 e7
mul r8   | 49 f7 e0
mul r8b  | 41 f6 e0
mul r8d  | 41 f7 e0
mul r8w  | 66 41 f7 e0
mul r9   | 49 f7 e1
mul r9b  | 41 f6 e1
mul r9d  | 41 f7 e1
mul r9w  | 66 41 f7 e1
mul rax  | 48 f7 e0
mul rbp  | 48 f7 e5
mul rbx  | 48 f7 e3
mul rcx  | 48 f7 e1
mul rdi  | 48 f7 e7
mul rdx  | 48 f7 e2
mul rsi  | 48 f7 e6
mul rsp  | 48 f7 e4
mul si   | 66 f7 e6
mul sil  | 40 f6 e6
mul sp   | 66 f7 e4
mul spl  | 40 f6 e4

# Or
or BYTE PTR [r11+r9*4+0x12345678],0x99        | 43 80 8c 8b 78 56 34 12 99
or BYTE PTR [rbx+r9*4+0x12345678],r9b         | 46 08 8c 8b 78 56 34 12
or DWORD PTR [r11+r9*4+0x12345678],0xdeadbeef | 43 81 8c 8b 78 56 34 12 ef be ad de
or QWORD PTR [r8],rdx                         | 49 09 10
or QWORD PTR [r9+rcx*4+0x12345678],rsi        | 49 09 b4 89 78 56 34 12
or WORD PTR [r11+r9*4+0x12345678],0xbeef      | 66 43 81 8c 8b 78 56 34 12 ef be
or al,0x12                                    | 0c 12
or cl,0x12                                    | 80 c9 12
or cx,0x1234                                  | 66 81 c9 34 12
or eax,0x00000012                             | 83 c8 12
or eax,0x12345678                             | 0d 78 56 34 12
or eax,DWORD PTR [rax+rbx*4+0x12345678]       | 0b 84 98 78 56 34 12
or ecx,DWORD PTR [r10]                        | 41 0b 0a
or edi,0x00000012                             | 83 cf 12
or rax,0x0000000000000012                     | 48 83 c8 12
or rax,0x0000000012345678                     | 48 0d 78 56 34 12
or rax,0xfffffffffffffffe                     | 48 83 c8 fe
or rax,QWORD PTR [rax+rbx*4+0x12345678]       | 48 0b 84 98 78 56 34 12
or rcx,QWORD PTR [r10]                        | 49 0b 0a
or rdi,0x0000000000000012                     | 48 83 cf 12

# Xor
xor cx,0x1234             | 66 81 f1 34 12
xor eax,0x00000012        | 83 f0 12
xor eax,0x12345678        | 35 78 56 34 12
xor ebx,0x12345678        | 81 f3 78 56 34 12
xor r8,0x0000000000000012 | 49 83 f0 12
xor r8,0x0000000012345678 | 49 81 f0 78 56 34 12

# Not
not eax  | f7 d0
not ebp  | f7 d5
not ebx  | f7 d3
not ecx  | f7 d1
not edi  | f7 d7
not edx  | f7 d2
not esi  | f7 d6
not esp  | f7 d4
not r10  | 49 f7 d2
not r10d | 41 f7 d2
not r11  | 49 f7 d3
not r11d | 41 f7 d3
not r12  | 49 f7 d4
not r12d | 41 f7 d4
not r13  | 49 f7 d5
not r13d | 41 f7 d5
not r14  | 49 f7 d6
not r14d | 41 f7 d6
not r15  | 49 f7 d7
not r15d | 41 f7 d7
not r8   | 49 f7 d0
not r8d  | 41 f7 d0
not r9   | 49 f7 d1
not r9d  | 41 f7 d1
not rax  | 48 f7 d0
not rbp  | 48 f7 d5
not rbx  | 48 f7 d3
not rcx  | 48 f7 d1
not rdi  | 48 f7 d7
not rdx  | 48 f7 d2
not rsi  | 48 f7 d6
not rsp  | 48 f7 d4

# Neg
neg DWORD PTR [r8+r9*4+0x12345678] | 43 f7 9c 88 78 56 34 12
neg QWORD PTR [r8+r9*4+0x12345678] | 4b f7 9c 88 78 56 34 12
neg eax                            | f7 d8
neg rbx                            | 48 f7 db

# Test
test BYTE PTR [r11+rdx*4+0x12345678],0x12         | 41 f6 84 93 78 56 34 12 12
test BYTE PTR [r11+rdx*4+0x12345678],0x99         | 41 f6 84 93 78 56 34 12 99
test BYTE PTR [r11+rdx*4+0x12345678],r13b         | 45 84 ac 93 78 56 34 12
test BYTE PTR [r11d+edx*4+0x12345678],0x99        | 67 41 f6 84 93 78 56 34 12 99
test BYTE PTR [r15+0x40],0x08                     | 41 f6 47 40 08
test DWORD PTR [r11+rdx*4+0x12345678],0xdeadbeef  | 41 f7 84 93 78 56 34 12 ef be ad de
test DWORD PTR [r11+rdx*4+0x12345678],ebx         | 41 85 9c 93 78 56 34 12
test DWORD PTR [r11d+edx*4+0x12345678],0xdeadbeef | 67 41 f7 84 93 78 56 34 12 ef be ad de
test QWORD PTR [r11+rdx*4+0x12345678],rax         | 49 85 84 93 78 56 34 12
test WORD PTR [r11+rdx*4+0x12345678],0x1234       | 66 41 f7 84 93 78 56 34 12 34 12
test WORD PTR [r11+rdx*4+0x12345678],r12w         | 66 45 85 a4 93 78 56 34 12
test al,0x12                                      | a8 12
test ax,0x1234                                    | 66 a9 34 12
test eax,0x12345678                               | a9 78 56 34 12
test r9b,0x12                                     | 41 f6 c1 12
test r9b,r9b                                      | 45 84 c9
test r9d,r9d                                      | 45 85 c9
test r9w,0x1234                                   | 66 41 f7 c1 34 12
test r9w,r9w                                      | 66 45 85 c9
test rax,0x12345678                               | 48 a9 78 56 34 12
test rbx,rbx                                      | 48 85 db

# Ud2
ud2 | 0f 0b

# Rep/repnz movs
movs BYTE PTR es:[rdi],BYTE PTR ds:[rsi]         | a4
movs DWORD PTR es:[rdi],DWORD PTR ds:[rsi]       | a5
movs WORD PTR es:[edi],WORD PTR ds:[esi]         | 67 66 a5
rep movs BYTE PTR es:[edi],BYTE PTR ds:[esi]     | 67 f3 a4
rep movs DWORD PTR es:[edi],DWORD PTR ds:[esi]   | 67 f3 a5
rep movs DWORD PTR es:[rdi],DWORD PTR ds:[rsi]   | f3 a5
rep movs WORD PTR es:[edi],WORD PTR ds:[esi]     | 67 66 f3 a5
rep movs WORD PTR es:[rdi],WORD PTR ds:[rsi]     | 66 f3 a5
repnz movs BYTE PTR es:[edi],BYTE PTR ds:[esi]   | 67 f2 a4
repnz movs DWORD PTR es:[edi],DWORD PTR ds:[esi] | 67 f2 a5
repnz movs WORD PTR es:[edi],WORD PTR ds:[esi]   | 67 66 f2 a5

# Rep stos
rep stos BYTE PTR es:[edi],al   | 67 f3 aa
rep stos BYTE PTR es:[rdi],al   | f3 aa
rep stos DWORD PTR es:[rdi],eax | f3 ab
rep stos QWORD PTR es:[edi],rax | 67 f3 48 ab
rep stos QWORD PTR es:[rdi],rax | f3 48 ab
stos BYTE PTR es:[edi],al       | 67 aa
stos QWORD PTR es:[edi],rax     | 67 48 ab

# Movdqa
movdqa xmm2,XMMWORD PTR [rsp+r9*4+0x12345678] | 66 42 0f 6f 94 8c 78 56 34 12

# Movaps
movaps XMMWORD PTR [rip+0x12345678],xmm6       | 0f 29 35 78 56 34 12
movaps XMMWORD PTR [rsp+r11*4+0x12345678],xmm7 | 42 0f 29 bc 9c 78 56 34 12
movaps xmm0,xmm0                               | 0f 28 c0
movaps xmm6,XMMWORD PTR [rip+0x12345678]       | 0f 28 35 78 56 34 12
movaps xmm7,XMMWORD PTR [rsp+r11*4+0x12345678] | 42 0f 28 bc 9c 78 56 34 12
movaps xmm7,xmm5                               | 0f 28 fd

# Movapd
movapd XMMWORD PTR [rip+0x12345678],xmm6       | 66 0f 29 35 78 56 34 12
movapd XMMWORD PTR [rsp+r11*4+0x12345678],xmm7 | 66 42 0f 29 bc 9c 78 56 34 12
movapd xmm0,xmm0                               | 66 0f 28 c0
movapd xmm7,xmm5                               | 66 0f 28 fd

# Movq
movq QWORD PTR [rbp+rsi*4+0x12345678],xmm3 | 66 0f d6 9c b5 78 56 34 12
movq QWORD PTR [rsi+0x12345678],xmm3       | 66 0f d6 9e 78 56 34 12
movq QWORD PTR [rsi],xmm3                  | 66 0f d6 1e
movq mm0,r9                                | 49 0f 6e c1
movq mm3,rsi                               | 48 0f 6e de
movq xmm0,r9                               | 66 49 0f 6e c1
movq xmm2,rax                              | 66 48 0f 6e d0
movq xmm3,QWORD PTR [rbp+rsi*4+0x12345678] | f3 0f 7e 9c b5 78 56 34 12
movq xmm6,QWORD PTR [rsi+0x12345678]       | f3 0f 7e b6 78 56 34 12
movq xmm6,QWORD PTR [rsi]                  | f3 0f 7e 36

# Movhps
movhps xmm3,QWORD PTR [eax]                  | 67 0f 16 18
movhps xmm3,QWORD PTR [rax]                  | 0f 16 18
movhps xmm3,QWORD PTR [rbp+rsi*4+0x12345678] | 0f 16 9c b5 78 56 34 12

# Movhlps
movhlps xmm0,xmm0 | 0f 12 c0
movhlps xmm3,xmm7 | 0f 12 df

# Punpcklqdq
punpcklqdq xmm0,xmm0 | 66 0f 6c c0
punpcklqdq xmm3,xmm9 | 66 41 0f 6c d9

# Punpckhqdq
punpckhqdq xmm0,xmm0 | 66 0f 6d c0
punpckhqdq xmm3,xmm9 | 66 41 0f 6d d9

# Punpckldq
punpckldq xmm0,xmm0 | 66 0f 62 c0
punpckldq xmm3,xmm9 | 66 41 0f 62 d9

# Setae
setae BYTE PTR [rdx+r9*2+0x12345678] | 42 0f 93 84 4a 78 56 34 12
setae al                             | 0f 93 c0
setae r8b                            | 41 0f 93 c0

# Setne
setne BYTE PTR [rdx+r9*2+0x12345678] | 42 0f 95 84 4a 78 56 34 12
setne al                             | 0f 95 c0
setne r8b                            | 41 0f 95 c0

# Setb
setb BYTE PTR [rdx+r9*2+0x12345678] | 42 0f 92 84 4a 78 56 34 12
setb al                             | 0f 92 c0
setb r8b                            | 41 0f 92 c0

# Sete
sete BYTE PTR [rdx+r9*2+0x12345678] | 42 0f 94 84 4a 78 56 34 12
sete al                             | 0f 94 c0
sete r8b                            | 41 0f 94 c0

# Seta
seta BYTE PTR [rdx+r9*2+0x12345678] | 42 0f 97 84 4a 78 56 34 12
seta al                             | 0f 97 c0
seta r8b                            | 41 0f 97 c0

# Setle
setle BYTE PTR [rdx+r9*2+0x12345678] | 42 0f 9e 84 4a 78 56 34 12
setle al                             | 0f 9e c0
setle r8b                            | 41 0f 9e c0

# Setbe
setbe BYTE PTR [rdx+r9*2+0x12345678] | 42 0f 96 84 4a 78 56 34 12
setbe al                             | 0f 96 c0
setbe r8b                            | 41 0f 96 c0

# Setl
setl BYTE PTR [rdx+r9*2+0x12345678] | 42 0f 9c 84 4a 78 56 34 12
setl al                             | 0f 9c c0
setl r8b                            | 41 0f 9c c0

# Setg
setg BYTE PTR [rdx+r9*2+0x12345678] | 42 0f 9f 84 4a 78 56 34 12
setg al                             | 0f 9f c0
setg r8b                            | 41 0f 9f c0

# Setge
setge BYTE PTR [rdx+r9*2+0x12345678] | 42 0f 9d 84 4a 78 56 34 12
setge al                             | 0f 9d c0
setge r8b                            | 41 0f 9d c0

# Movabs
movabs rcx,0xdeadbeef00000000 | 48 b9 00 00 00 00 ef be ad de
movabs rdx,0xdeadbeefcafebabe | 48 ba be ba fe ca ef be ad de

# Movups
movups XMMWORD PTR [ebx+edi*8+0x12345678],xmm14 | 67 44 0f 11 b4 fb 78 56 34 12
movups XMMWORD PTR [r8],xmm0                    | 41 0f 11 00
movups XMMWORD PTR [rbx+rdi*8+0x12345678],xmm14 | 44 0f 11 b4 fb 78 56 34 12
movups xmm0,XMMWORD PTR [rbx]                   | 0f 10 03

# Movsd
movsd xmm0,QWORD PTR [r8]                    | f2 41 0f 10 00
movsd xmm0,QWORD PTR [rbx]                   | f2 0f 10 03
movsd xmm14,QWORD PTR [ebx+edi*8+0x12345678] | 67 f2 44 0f 10 b4 fb 78 56 34 12
movsd xmm14,QWORD PTR [rbx+rdi*8+0x12345678] | f2 44 0f 10 b4 fb 78 56 34 12

# Endbr64
endbr64 | f3 0f 1e fa

# Inc
inc ah   | fe c4
inc al   | fe c0
inc bh   | fe c7
inc bl   | fe c3
inc bpl  | 40 fe c5
inc ch   | fe c5
inc cl   | fe c1
inc dh   | fe c6
inc dil  | 40 fe c7
inc dl   | fe c2
inc eax  | ff c0
inc ebp  | ff c5
inc ebx  | ff c3
inc ecx  | ff c1
inc edi  | ff c7
inc edx  | ff c2
inc esi  | ff c6
inc esp  | ff c4
inc r10  | 49 ff c2
inc r10b | 41 fe c2
inc r10d | 41 ff c2
inc r11  | 49 ff c3
inc r11b | 41 fe c3
inc r11d | 41 ff c3
inc r12  | 49 ff c4
inc r12b | 41 fe c4
inc r12d | 41 ff c4
inc r13  | 49 ff c5
inc r13b | 41 fe c5
inc r13d | 41 ff c5
inc r14  | 49 ff c6
inc r14b | 41 fe c6
inc r14d | 41 ff c6
inc r15  | 49 ff c7
inc r15b | 41 fe c7
inc r15d | 41 ff c7
inc r8   | 49 ff c0
inc r8b  | 41 fe c0
inc r8d  | 41 ff c0
inc r9   | 49 ff c1
inc r9b  | 41 fe c1
inc r9d  | 41 ff c1
inc rax  | 48 ff c0
inc rbp  | 48 ff c5
inc rbx  | 48 ff c3
inc rcx  | 48 ff c1
inc rdi  | 48 ff c7
inc rdx  | 48 ff c2
inc rsi  | 48 ff c6
inc rsp  | 48 ff c4
inc sil  | 40 fe c6
inc spl  | 40 fe c4
#
inc BYTE PTR [rax+0x12345678]        | fe 80 78 56 34 12
inc BYTE PTR [rax]                   | fe 00
inc BYTE PTR [rbx+rcx*2+0x12345678]  | fe 84 4b 78 56 34 12
inc DWORD PTR [rax+0x12345678]       | ff 80 78 56 34 12
inc DWORD PTR [rax]                  | ff 00
inc DWORD PTR [rbp+0x12345678]       | ff 85 78 56 34 12
inc DWORD PTR [rbp+rsi*2+0x12345678] | ff 84 75 78 56 34 12
inc DWORD PTR [rbx+0x12345678]       | ff 83 78 56 34 12
inc DWORD PTR [rcx+0x12345678]       | ff 81 78 56 34 12
inc DWORD PTR [rdi+0x12345678]       | ff 87 78 56 34 12
inc DWORD PTR [rdx+0x12345678]       | ff 82 78 56 34 12
inc DWORD PTR [rsi+0x12345678]       | ff 86 78 56 34 12
inc DWORD PTR [rsp+0x12345678]       | ff 84 24 78 56 34 12
inc QWORD PTR [r8+rdi*2+0x12345678]  | 49 ff 84 78 78 56 34 12
inc QWORD PTR [rax]                  | 48 ff 00
inc QWORD PTR [rcx+0x12345678]       | 48 ff 81 78 56 34 12
inc WORD PTR [rax+0x12345678]        | 66 ff 80 78 56 34 12
inc WORD PTR [rax]                   | 66 ff 00
inc WORD PTR [rdx+rdi*2+0x12345678]  | 66 ff 84 7a 78 56 34 12

# Dec
dec ah   | fe cc
dec al   | fe c8
dec bh   | fe cf
dec bl   | fe cb
dec bpl  | 40 fe cd
dec ch   | fe cd
dec cl   | fe c9
dec dh   | fe ce
dec dil  | 40 fe cf
dec dl   | fe ca
dec eax  | ff c8
dec ebp  | ff cd
dec ebx  | ff cb
dec ecx  | ff c9
dec edi  | ff cf
dec edx  | ff ca
dec esi  | ff ce
dec esp  | ff cc
dec r10  | 49 ff ca
dec r10b | 41 fe ca
dec r10d | 41 ff ca
dec r11  | 49 ff cb
dec r11b | 41 fe cb
dec r11d | 41 ff cb
dec r12  | 49 ff cc
dec r12b | 41 fe cc
dec r12d | 41 ff cc
dec r13  | 49 ff cd
dec r13b | 41 fe cd
dec r13d | 41 ff cd
dec r14  | 49 ff ce
dec r14b | 41 fe ce
dec r14d | 41 ff ce
dec r15  | 49 ff cf
dec r15b | 41 fe cf
dec r15d | 41 ff cf
dec r8   | 49 ff c8
dec r8b  | 41 fe c8
dec r8d  | 41 ff c8
dec r9   | 49 ff c9
dec r9b  | 41 fe c9
dec r9d  | 41 ff c9
dec rax  | 48 ff c8
dec rbp  | 48 ff cd
dec rbx  | 48 ff cb
dec rcx  | 48 ff c9
dec rdi  | 48 ff cf
dec rdx  | 48 ff ca
dec rsi  | 48 ff ce
dec rsp  | 48 ff cc
dec sil  | 40 fe ce
dec spl  | 40 fe cc
#
dec BYTE PTR [rax+0x12345678]        | fe 88 78 56 34 12
dec BYTE PTR [rax]                   | fe 08
dec BYTE PTR [rbx+rcx*2+0x12345678]  | fe 8c 4b 78 56 34 12
dec DWORD PTR [rbp+rsi*2+0x12345678] | ff 8c 75 78 56 34 12
dec DWORD PTR [rsp+0x12345678]       | ff 8c 24 78 56 34 12
dec DWORD PTR [rsp]                  | ff 0c 24
dec QWORD PTR [r8+rdi*2+0x12345678]  | 49 ff 8c 78 78 56 34 12
dec QWORD PTR [rax]                  | 48 ff 08
dec QWORD PTR [rcx+0x12345678]       | 48 ff 89 78 56 34 12
dec WORD PTR [rax*2+0x0]             | 66 ff 0c 45 00 00 00 00
dec WORD PTR [rax+0x12345678]        | 66 ff 88 78 56 34 12
dec WORD PTR [rax+rbx*1+0x0]         | 66 ff 0c 18
dec WORD PTR [rax]                   | 66 ff 08
dec WORD PTR [rdx+rdi*2+0x12345678]  | 66 ff 8c 7a 78 56 34 12

# Pshufd
pshufd xmm0,xmm1,0x12 | 66 0f 70 c1 12

# Pshufw
pshufw mm0,mm1,0x12 | 0f 70 c1 12

# Shufpd
shufpd xmm0,xmm1,0x12 | 66 0f c6 c1 12

# Shufps
shufps xmm0,xmm1,0x12 | 0f c6 c1 12

# Pxor
pxor xmm1,xmm15                              | 66 41 0f ef cf
pxor xmm4,XMMWORD PTR [rax+r11*4+0x12345678] | 66 42 0f ef a4 98 78 56 34 12
pxor xmm7,xmm7                               | 66 0f ef ff

# Por
por xmm1,xmm15                              | 66 41 0f eb cf
por xmm4,XMMWORD PTR [rax+r11*4+0x12345678] | 66 42 0f eb a4 98 78 56 34 12
por xmm7,xmm7                               | 66 0f eb ff

# Pand
pand xmm1,xmm15                              | 66 41 0f db cf
pand xmm4,XMMWORD PTR [rax+r11*4+0x12345678] | 66 42 0f db a4 98 78 56 34 12
pand xmm7,xmm7                               | 66 0f db ff

# Paddq
paddq xmm1,xmm15                              | 66 41 0f d4 cf
paddq xmm4,XMMWORD PTR [rax+r11*4+0x12345678] | 66 42 0f d4 a4 98 78 56 34 12
paddq xmm4,XMMWORD PTR [rdi]                  | 66 0f d4 27
paddq xmm7,xmm7                               | 66 0f d4 ff

# Psubq
psubq xmm1,xmm15                              | 66 41 0f fb cf
psubq xmm4,XMMWORD PTR [rax+r11*4+0x12345678] | 66 42 0f fb a4 98 78 56 34 12
psubq xmm4,XMMWORD PTR [rdi]                  | 66 0f fb 27
psubq xmm7,xmm7                               | 66 0f fb ff

# Cvtsi2sd
cvtsi2sd xmm2,rdi | f2 48 0f 2a d7
cvtsi2sd xmm8,eax | f2 44 0f 2a c0

# Divsd
divsd xmm0,xmm0  | f2 0f 5e c0
divsd xmm8,xmm11 | f2 45 0f 5e c3

# Addsd
addsd xmm0,xmm0  | f2 0f 58 c0
addsd xmm8,xmm11 | f2 45 0f 58 c3

# Xorps
xorps xmm0,xmm0  | 0f 57 c0
xorps xmm8,xmm11 | 45 0f 57 c3

# Ucomisd
ucomisd xmm13,QWORD PTR [rip+0x12345678] | 66 44 0f 2e 2d 78 56 34 12

# Ucomiss
ucomiss xmm13,DWORD PTR [rip+0x12345678] | 44 0f 2e 2d 78 56 34 12

# BTx
bt edx,0x12  | 0f ba e2 12
bt edx,esi   | 0f a3 f2
bt rdx,0x12  | 48 0f ba e2 12
bt rdx,rdi   | 48 0f a3 fa
btc ecx,0x12 | 0f ba f9 12
btc ecx,r9d  | 44 0f bb c9
btc rcx,0x12 | 48 0f ba f9 12
btc rcx,r10  | 4c 0f bb d1
btr ebx,0x12 | 0f ba f3 12
btr ebx,r11d | 44 0f b3 db
btr rbx,0x12 | 48 0f ba f3 12
btr rbx,r12  | 4c 0f b3 e3
bts eax,0x12 | 0f ba e8 12
bts eax,r13d | 44 0f ab e8
bts rax,0x12 | 48 0f ba e8 12
bts rax,r14  | 4c 0f ab f0

# Xgetbv
xgetbv | 0f 01 d0

# Xchg
xchg al,cl    | 86 c8
xchg bh,cl    | 86 cf
xchg di,ax    | 66 97
xchg ebp,eax  | 95
xchg ebx,eax  | 93
xchg ebx,r9d  | 44 87 cb
xchg ecx,eax  | 91
xchg edi,eax  | 97
xchg edx,eax  | 92
xchg esi,eax  | 96
xchg esp,eax  | 94
xchg r10,rax  | 49 92
xchg r10d,eax | 41 92
xchg r11,rax  | 49 93
xchg r11d,eax | 41 93
xchg r12,rax  | 49 94
xchg r12d,eax | 41 94
xchg r13,rax  | 49 95
xchg r13d,eax | 41 95
xchg r14,rax  | 49 96
xchg r14d,eax | 41 96
xchg r15,rax  | 49 97
xchg r15d,eax | 41 97
xchg r8,rax   | 49 90
xchg r8d,eax  | 41 90
xchg r9,rax   | 49 91
xchg r9d,eax  | 41 91
xchg rbp,rax  | 48 95
xchg rbx,r9   | 4c 87 cb
xchg rbx,rax  | 48 93
xchg rbx,rcx  | 48 87 cb
xchg rcx,rax  | 48 91
xchg rdi,rax  | 48 97
xchg rdx,rax  | 48 92
xchg rsi,rax  | 48 96
xchg rsp,rax  | 48 94
xchg si,di    | 66 87 fe
#
xchg BYTE PTR [rax+rbx*2+0x12345678],ah   | 86 a4 58 78 56 34 12
xchg BYTE PTR [rax+rbx*2+0x12345678],al   | 86 84 58 78 56 34 12
xchg DWORD PTR [rax+rbx*2+0x12345678],eax | 87 84 58 78 56 34 12
xchg QWORD PTR [rax+rbx*2+0x12345678],rax | 48 87 84 58 78 56 34 12
xchg WORD PTR [rax+rbx*2+0x12345678],ax   | 66 87 84 58 78 56 34 12

# Bswap
bswap eax  | 0f c8
bswap ebp  | 0f cd
bswap ebx  | 0f cb
bswap ecx  | 0f c9
bswap edi  | 0f cf
bswap edx  | 0f ca
bswap esi  | 0f ce
bswap esp  | 0f cc
bswap r10  | 49 0f ca
bswap r10d | 41 0f ca
bswap r11  | 49 0f cb
bswap r11d | 41 0f cb
bswap r12  | 49 0f cc
bswap r12d | 41 0f cc
bswap r13  | 49 0f cd
bswap r13d | 41 0f cd
bswap r14  | 49 0f ce
bswap r14d | 41 0f ce
bswap r15  | 49 0f cf
bswap r15d | 41 0f cf
bswap r8   | 49 0f c8
bswap r8d  | 41 0f c8
bswap r9   | 49 0f c9
bswap r9d  | 41 0f c9
bswap rax  | 48 0f c8
bswap rbp  | 48 0f cd
bswap rbx  | 48 0f cb
bswap rcx  | 48 0f c9
bswap rdi  | 48 0f cf
bswap rdx  | 48 0f ca
bswap rsi  | 48 0f ce
bswap rsp  | 48 0f cc

# Prefetch
prefetchnta BYTE PTR [eax]                   | 67 0f 18 00
prefetchnta BYTE PTR [ebx+r11d*4+0x12345678] | 67 42 0f 18 84 9b 78 56 34 12
prefetchnta BYTE PTR [r9+r11*4+0x12345678]   | 43 0f 18 84 99 78 56 34 12
prefetchnta BYTE PTR [r9+rcx*4+0x12345678]   | 41 0f 18 84 89 78 56 34 12
prefetchnta BYTE PTR [r9d+ecx*4+0x12345678]  | 67 41 0f 18 84 89 78 56 34 12
prefetchnta BYTE PTR [r9d+r11d*4+0x12345678] | 67 43 0f 18 84 99 78 56 34 12
prefetchnta BYTE PTR [rax]                   | 0f 18 00
prefetchnta BYTE PTR [rbx+r11*4+0x12345678]  | 42 0f 18 84 9b 78 56 34 12
prefetcht0 BYTE PTR [eax]                    | 67 0f 18 08
prefetcht0 BYTE PTR [ebx+r11d*4+0x12345678]  | 67 42 0f 18 8c 9b 78 56 34 12
prefetcht0 BYTE PTR [r9+r11*4+0x12345678]    | 43 0f 18 8c 99 78 56 34 12
prefetcht0 BYTE PTR [r9+rcx*4+0x12345678]    | 41 0f 18 8c 89 78 56 34 12
prefetcht0 BYTE PTR [r9d+ecx*4+0x12345678]   | 67 41 0f 18 8c 89 78 56 34 12
prefetcht0 BYTE PTR [r9d+r11d*4+0x12345678]  | 67 43 0f 18 8c 99 78 56 34 12
prefetcht0 BYTE PTR [rax]                    | 0f 18 08
prefetcht0 BYTE PTR [rbx+r11*4+0x12345678]   | 42 0f 18 8c 9b 78 56 34 12
prefetcht1 BYTE PTR [eax]                    | 67 0f 18 10
prefetcht1 BYTE PTR [ebx+r11d*4+0x12345678]  | 67 42 0f 18 94 9b 78 56 34 12
prefetcht1 BYTE PTR [r9+r11*4+0x12345678]    | 43 0f 18 94 99 78 56 34 12
prefetcht1 BYTE PTR [r9+rcx*4+0x12345678]    | 41 0f 18 94 89 78 56 34 12
prefetcht1 BYTE PTR [r9d+ecx*4+0x12345678]   | 67 41 0f 18 94 89 78 56 34 12
prefetcht1 BYTE PTR [r9d+r11d*4+0x12345678]  | 67 43 0f 18 94 99 78 56 34 12
prefetcht1 BYTE PTR [rax]                    | 0f 18 10
prefetcht1 BYTE PTR [rbx+r11*4+0x12345678]   | 42 0f 18 94 9b 78 56 34 12
prefetcht2 BYTE PTR [eax]                    | 67 0f 18 18
prefetcht2 BYTE PTR [ebx+r11d*4+0x12345678]  | 67 42 0f 18 9c 9b 78 56 34 12
prefetcht2 BYTE PTR [r9+r11*4+0x12345678]    | 43 0f 18 9c 99 78 56 34 12
prefetcht2 BYTE PTR [r9+rcx*4+0x12345678]    | 41 0f 18 9c 89 78 56 34 12
prefetcht2 BYTE PTR [r9d+ecx*4+0x12345678]   | 67 41 0f 18 9c 89 78 56 34 12
prefetcht2 BYTE PTR [r9d+r11d*4+0x12345678]  | 67 43 0f 18 9c 99 78 56 34 12
prefetcht2 BYTE PTR [rax]                    | 0f 18 18
prefetcht2 BYTE PTR [rbx+r11*4+0x12345678]   | 42 0f 18 9c 9b 78 56 34 12

# Cmpxchg
cmpxchg BYTE PTR [rax+rbx*4+0x12345678],dh         | 0f b0 b4 98 78 56 34 12
cmpxchg BYTE PTR [rsi],bpl                         | 40 0f b0 2e
cmpxchg DWORD PTR [rax+rbx*4+0x12345678],r10d      | 44 0f b1 94 98 78 56 34 12
cmpxchg DWORD PTR [rsi],ecx                        | 0f b1 0e
cmpxchg QWORD PTR [rax+rbx*4+0x12345678],rdi       | 48 0f b1 bc 98 78 56 34 12
cmpxchg QWORD PTR [rsi],r9                         | 4c 0f b1 0e
cmpxchg WORD PTR [rax+rbx*4+0x12345678],r15w       | 66 44 0f b1 bc 98 78 56 34 12
cmpxchg WORD PTR [rsi],dx                          | 66 0f b1 16
lock cmpxchg BYTE PTR [rax+rbx*4+0x12345678],dh    | f0 0f b0 b4 98 78 56 34 12
lock cmpxchg BYTE PTR [rsi],bpl                    | f0 40 0f b0 2e
lock cmpxchg DWORD PTR [rax+rbx*4+0x12345678],r10d | f0 44 0f b1 94 98 78 56 34 12
lock cmpxchg DWORD PTR [rsi],ecx                   | f0 0f b1 0e
lock cmpxchg QWORD PTR [rax+rbx*4+0x12345678],rdi  | f0 48 0f b1 bc 98 78 56 34 12
lock cmpxchg QWORD PTR [rsi],r9                    | f0 4c 0f b1 0e
lock cmpxchg WORD PTR [rax+rbx*4+0x12345678],r15w  | 66 f0 44 0f b1 bc 98 78 56 34 12
lock cmpxchg WORD PTR [rsi],dx                     | 66 f0 0f b1 16

# Xadd
lock xadd BYTE PTR [rax+rbx*4+0x12345678],dh    | f0 0f c0 b4 98 78 56 34 12
lock xadd BYTE PTR [rsi],bpl                    | f0 40 0f c0 2e
lock xadd DWORD PTR [rax+rbx*4+0x12345678],r10d | f0 44 0f c1 94 98 78 56 34 12
lock xadd DWORD PTR [rsi],ecx                   | f0 0f c1 0e
lock xadd QWORD PTR [rax+rbx*4+0x12345678],rdi  | f0 48 0f c1 bc 98 78 56 34 12
lock xadd QWORD PTR [rsi],r9                    | f0 4c 0f c1 0e
lock xadd WORD PTR [rax+rbx*4+0x12345678],r15w  | 66 f0 44 0f c1 bc 98 78 56 34 12
lock xadd WORD PTR [rsi],dx                     | 66 f0 0f c1 16
xadd BYTE PTR [rax+rbx*4+0x12345678],dh         | 0f c0 b4 98 78 56 34 12
xadd BYTE PTR [rsi],bpl                         | 40 0f c0 2e
xadd DWORD PTR [rax+rbx*4+0x12345678],r10d      | 44 0f c1 94 98 78 56 34 12
xadd DWORD PTR [rsi],ecx                        | 0f c1 0e
xadd QWORD PTR [rax+rbx*4+0x12345678],rdi       | 48 0f c1 bc 98 78 56 34 12
xadd QWORD PTR [rsi],r9                         | 4c 0f c1 0e
xadd WORD PTR [rax+rbx*4+0x12345678],r15w       | 66 44 0f c1 bc 98 78 56 34 12
xadd WORD PTR [rsi],dx                          | 66 0f c1 16

# Pcmpeqd
pcmpeqd xmm0,xmm0  | 66 0f 76 c0
pcmpeqd xmm3,xmm11 | 66 41 0f 76 db

# Rdrand
rdrand ax   | 66 0f c7 f0
rdrand eax  | 0f c7 f0
rdrand r11  | 49 0f c7 f3
rdrand r12d | 41 0f c7 f4
rdrand r13w | 66 41 0f c7 f5
rdrand rax  | 48 0f c7 f0

# Rdseed
rdseed ax   | 66 0f c7 f8
rdseed eax  | 0f c7 f8
rdseed r11  | 49 0f c7 fb
rdseed r12d | 41 0f c7 fc
rdseed r13w | 66 41 0f c7 fd
rdseed rax  | 48 0f c7 f8

# Rdsspq
rdsspq r11 | f3 49 0f 1e cb
rdsspq rax | f3 48 0f 1e c8

# Incsspq
incsspq r11 | f3 49 0f ae eb
incsspq rax | f3 48 0f ae e8

# Lahf
lahf | 9f

# Sahf
sahf | 9e
