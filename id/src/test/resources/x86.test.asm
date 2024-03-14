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
call DWORD PTR [ebx]                    | 67 66 ff 1b
call DWORD PTR [r11d+r12d*4+0x12345678] | 67 66 43 ff 9c a3 78 56 34 12
call DWORD PTR [rsp]                    | 66 ff 1c 24
call QWORD PTR [eax]                    | 67 ff 10
call QWORD PTR [rdx]                    | ff 12
call WORD PTR [ecx]                     | 67 66 ff 11
call WORD PTR [rsi]                     | 66 ff 16

# Cdq
cdq | 99

# Cwde
cwde | 98

# Cdqe
cdqe | 48 98

# Ja
# The output of these instructions is different from what you can see from other tools such as objdump
# because here we keep the addition to the instruction pointer implicit.
# In reality, it would look like 'jg rip+0x....'
ja 0x12       | 77 12
ja 0x78563412 | 0f 87 12 34 56 78

# Jbe
# The output of these instructions is different from what you can see from other tools such as objdump
# because here we keep the addition to the instruction pointer implicit.
# In reality, it would look like 'jbe rip+0x....'
jbe 0x78563412 | 0f 86 12 34 56 78

# Jg
# The output of these instructions is different from what you can see from other tools such as objdump
# because here we keep the addition to the instruction pointer implicit.
# In reality, it would look like 'jg rip+0x....'
jg 0x12       | 7f 12
jg 0x78563412 | 0f 8f 12 34 56 78

# Je
# The output of these instructions is different from what you can see from other tools such as objdump
# because here we keep the addition to the instruction pointer implicit.
# In reality, it would look like 'je rip+0x....'
je 0x12       | 74 12
je 0x2e0db    | 0f 84 db e0 02 00
je 0x78563412 | 0f 84 12 34 56 78

# Jle
# The output of these instructions is different from what you can see from other tools such as objdump
# because here we keep the addition to the instruction pointer implicit.
# In reality, it would look like 'jle rip+0x....'
jle 0x12       | 7e 12
jle 0x12345678 | 0f 8e 78 56 34 12

# Jne
# The output of these instructions is different from what you can see from other tools such as objdump
# because here we keep the addition to the instruction pointer implicit.
# In reality, it would look like 'jne rip+0x....'
jne 0x12       | 75 12
jne 0x12345678 | 0f 85 78 56 34 12

# Jns
# The output of these instructions is different from what you can see from other tools such as objdump
# because here we keep the addition to the instruction pointer implicit.
# In reality, it would look like 'jns rip+0x....'
jns 0x12       | 79 12
jns 0x12345678 | 0f 89 78 56 34 12

# Js
# The output of these instructions is different from what you can see from other tools such as objdump
# because here we keep the addition to the instruction pointer implicit.
# In reality, it would look like 'js rip+0x....'
js 0x12       | 78 12
js 0x12345678 | 0f 88 78 56 34 12

# Jmp
# The output of these instructions is different from what you can see from other tools such as objdump
# because here we keep the addition to the instruction pointer implicit.
# In reality, it would look like 'jmp rip+0x....'
jmp 0x12       | eb 12
jmp 0x78563412 | e9 12 34 56 78

# Cmove
cmove r15,rcx | 4c 0f 44 f9
cmove rcx,r15 | 49 0f 44 cf

# Cmovbe
cmovbe r15,rcx | 4c 0f 46 f9
cmovbe rcx,r15 | 49 0f 46 cf

# Cmp
cmp BYTE PTR [r9+rcx*4+0x12345678],0x99        | 41 80 bc 89 78 56 34 12 99
cmp DWORD PTR [r9+rcx*4+0x12345678],0xdeadbeef | 41 81 bc 89 78 56 34 12 ef be ad de
cmp DWORD PTR [rbp-0xe8],r15d                  | 44 39 bd 18 ff ff ff
cmp DWORD PTR [rdi],0x12345678                 | 81 3f 78 56 34 12
cmp QWORD PTR [rdi],0x12345678                 | 48 81 3f 78 56 34 12
cmp WORD PTR [r9+rcx*4+0x12345678],0xbeef      | 66 41 81 bc 89 78 56 34 12 ef be
cmp al,dh                                      | 38 f0
cmp eax,0x12345678                             | 3d 78 56 34 12
cmp ebp,DWORD PTR [rbx+r9*4+0x12345678]        | 42 3b ac 8b 78 56 34 12
cmp edi,0x12345678                             | 81 ff 78 56 34 12
cmp esp,r13d                                   | 44 39 ec
cmp r8w,dx                                     | 66 41 39 d0
cmp rax,0x12345678                             | 48 3d 78 56 34 12
cmp rdi,0x12345678                             | 48 81 ff 78 56 34 12
cmp rsp,r8                                     | 4c 39 c4
cmp sp,r13w                                    | 66 44 39 ec

# Lea
lea ax,[ebx+ecx*4+0x12345678]   | 67 66 8d 84 8b 78 56 34 12
lea cx,[rbx+rcx*4+0x12345678]   | 66 8d 8c 8b 78 56 34 12
lea eax,[ebx]                   | 67 8d 03
lea eax,[rbx]                   | 8d 03
lea ecx,[rdx+rbp*2]             | 8d 0c 6a
lea esi,[edi+r12d*2+0x12345678] | 67 42 8d b4 67 78 56 34 12
lea r10w,[ebx+ecx*4+0x12345678] | 66 67 44 8d 94 8b 78 56 34 12
lea r10w,[ebx+ecx*4+0x12345678] | 67 66 44 8d 94 8b 78 56 34 12
lea r13d,[rdi+r8*4+0x12345678]  | 46 8d ac 87 78 56 34 12
lea r14w,[rbx+rcx*4+0x12345678] | 66 44 8d b4 8b 78 56 34 12
lea r9d,[edx+ebp*2]             | 67 44 8d 0c 6a
lea rax,[ebx]                   | 67 48 8d 03
lea rax,[rbx]                   | 48 8d 03
lea rcx,[edx+ebp*2]             | 67 48 8d 0c 6a
lea rcx,[rdx+rbp*2]             | 48 8d 0c 6a
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
mov BYTE PTR [rsp+rcx*4+0x12345678],bh         | 88 bc 8c 78 56 34 12
mov BYTE PTR [rsp+rcx*4+0x12345678],cl         | 88 8c 8c 78 56 34 12
mov BYTE PTR [rsp+rcx*4+0x12345678],dil        | 40 88 bc 8c 78 56 34 12
mov BYTE PTR [rsp+rcx*4+0x12345678],r9b        | 44 88 8c 8c 78 56 34 12
mov DWORD PTR [r11+r8*4+0x12345678],0xdeadbeef | 43 c7 84 83 78 56 34 12 ef be ad de
mov DWORD PTR [rbp+0x7eadbeef],0x12345678      | c7 85 ef be ad 7e 78 56 34 12
mov QWORD PTR [rbp+0x7eadbeef],0x12345678      | 48 c7 85 ef be ad 7e 78 56 34 12
mov QWORD PTR [rbp+r9*4+0x12345678],rsi        | 4a 89 b4 8d 78 56 34 12
mov WORD PTR [r11+r8*4+0x12345678],0xbeef      | 66 43 c7 84 83 78 56 34 12 ef be
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
movsxd rdx,DWORD PTR [r11+r15*4+0x12345678] | 4b 63 94 bb 78 56 34 12

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
add DWORD PTR [eax+ebx*4+0x12345678],r8d  | 67 44 01 84 98 78 56 34 12
add DWORD PTR [rax+rbx*4+0x12345678],r8d  | 44 01 84 98 78 56 34 12
add QWORD PTR [rax+rbx*4+0x12345678],r9   | 4c 01 8c 98 78 56 34 12
add QWORD PTR [rax+rbx*4+0x12345678],rsp  | 48 01 a4 98 78 56 34 12
add WORD PTR [rax+rbx*4+0x12345678],r8w   | 66 44 01 84 98 78 56 34 12
add al,0x99                               | 04 99
add eax,0x18                              | 83 c0 18
add esp,DWORD PTR [rax+rbx*4+0x12345678]  | 03 a4 98 78 56 34 12
add r11d,DWORD PTR [rax+rbx*4+0x12345678] | 44 03 9c 98 78 56 34 12
add r8,0x1                                | 49 83 c0 01
add r8,r9                                 | 4d 01 c8
add r9,QWORD PTR [rax+rbx*4+0x12345678]   | 4c 03 8c 98 78 56 34 12
add rax,0x1                               | 48 83 c0 01
add rsp,0x12345678                        | 48 81 c4 78 56 34 12
add rsp,QWORD PTR [rax+rbx*4+0x12345678]  | 48 03 a4 98 78 56 34 12

# And
and edi,0x1                | 83 e7 01
and edi,0x1d               | 83 e7 1d
and edi,0x7f               | 83 e7 7f
and edi,0xf                | 83 e7 0f
and edx,0xfff              | 81 e2 ff 0f 00 00
and r12,r13                | 4d 21 ec
and r15d,0x1f              | 41 83 e7 1f
and rdi,0xfffffffffffffff0 | 48 83 e7 f0

# Sub
sub DWORD PTR [eax+ebx*4+0x12345678],r8d  | 67 44 29 84 98 78 56 34 12
sub DWORD PTR [rax+rbx*4+0x12345678],r8d  | 44 29 84 98 78 56 34 12
sub QWORD PTR [rax+rbx*4+0x12345678],r9   | 4c 29 8c 98 78 56 34 12
sub QWORD PTR [rax+rbx*4+0x12345678],rsp  | 48 29 a4 98 78 56 34 12
sub WORD PTR [rax+rbx*4+0x12345678],r8w   | 66 44 29 84 98 78 56 34 12
sub esi,0x12                              | 83 ee 12
sub esp,DWORD PTR [rax+rbx*4+0x12345678]  | 2b a4 98 78 56 34 12
sub r11d,DWORD PTR [rax+rbx*4+0x12345678] | 44 2b 9c 98 78 56 34 12
sub r8,r9                                 | 4d 29 c8
sub r9,QWORD PTR [rax+rbx*4+0x12345678]   | 4c 2b 8c 98 78 56 34 12
sub rdi,0x12                              | 48 83 ef 12
sub rsp,0x12345678                        | 48 81 ec 78 56 34 12
sub rsp,QWORD PTR [rax+rbx*4+0x12345678]  | 48 2b a4 98 78 56 34 12

# Sbb
sbb al,0x12   | 1c 12
sbb esi,esi   | 19 f6
sbb r12d,r12d | 45 19 e4
sbb rax,rax   | 48 19 c0

# Shr
shr bpl,0x1  | 40 d0 ed
shr ecx,0x12 | c1 e9 12
shr rdx,0x12 | 48 c1 ea 12
shr sil,0x1  | 40 d0 ee
shr spl,0x1  | 40 d0 ec

# Shl
shl esi,0x12 | c1 e6 12
shl r9b,0x12 | 41 c0 e1 12
shl rdx,0x99 | 48 c1 e2 99

# Imul
imul eax,ebx,0x12 | 6b c3 12
imul rbx,rbp      | 48 0f af dd
imul rdx,r9,0x58  | 49 6b d1 58

# Or
or BYTE PTR [r11+r9*4+0x12345678],0x99        | 43 80 8c 8b 78 56 34 12 99
or DWORD PTR [r11+r9*4+0x12345678],0xdeadbeef | 43 81 8c 8b 78 56 34 12 ef be ad de
or WORD PTR [r11+r9*4+0x12345678],0xbeef      | 66 43 81 8c 8b 78 56 34 12 ef be

# Xor
xor eax,0x12       | 83 f0 12
xor eax,0x12345678 | 35 78 56 34 12
xor r8,0x12        | 49 83 f0 12
xor r8,0x12345678  | 49 81 f0 78 56 34 12

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

# Test
test BYTE PTR [r11+rdx*4+0x12345678],0x99         | 41 f6 84 93 78 56 34 12 99
test BYTE PTR [r11d+edx*4+0x12345678],0x99        | 67 41 f6 84 93 78 56 34 12 99
test BYTE PTR [r15+0x40],0x8                      | 41 f6 47 40 08
test DWORD PTR [r11+rdx*4+0x12345678],0xdeadbeef  | 41 f7 84 93 78 56 34 12 ef be ad de
test DWORD PTR [r11d+edx*4+0x12345678],0xdeadbeef | 67 41 f7 84 93 78 56 34 12 ef be ad de
test al,0x12                                      | a8 12
test eax,0x12345678                               | a9 78 56 34 12
test r9b,r9b                                      | 45 84 c9
test r9d,r9d                                      | 45 85 c9
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

# Movdqa
movdqa xmm2,XMMWORD PTR [rsp+r9*4+0x12345678] | 66 42 0f 6f 94 8c 78 56 34 12

# Movaps
movaps XMMWORD PTR [rip+0x12345678],xmm6       | 0f 29 35 78 56 34 12
movaps XMMWORD PTR [rsp+r11*4+0x12345678],xmm7 | 42 0f 29 bc 9c 78 56 34 12

# Movq
movq mm0,r9   | 49 0f 6e c1
movq mm3,rsi  | 48 0f 6e de
movq xmm0,r9  | 66 49 0f 6e c1
movq xmm2,rax | 66 48 0f 6e d0

# Punpcklqdq
punpcklqdq xmm0,xmm0 | 66 0f 6c c0
punpcklqdq xmm3,xmm9 | 66 41 0f 6c d9

# Setne
setne al  | 0f 95 c0
setne r8b | 41 0f 95 c0

# Movabs
movabs rcx,0xdeadbeef         | 48 b9 ef be ad de 00 00 00 00
movabs rdx,0xdeadbeefcafebabe | 48 ba be ba fe ca ef be ad de

# Movups
movups XMMWORD PTR [ebx+edi*8+0x12345678],xmm14 | 67 44 0f 11 b4 fb 78 56 34 12
movups XMMWORD PTR [r8],xmm0                    | 41 0f 11 00
movups XMMWORD PTR [rbx+rdi*8+0x12345678],xmm14 | 44 0f 11 b4 fb 78 56 34 12
