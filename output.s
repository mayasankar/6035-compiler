.globl main

.comm a, 80, 8
inc:
enter $704, $0
mov %rdi, -32(%rbp)

.inc_0:
mov -32(%rbp), %r10
mov %r10, -8(%rbp)
mov -8(%rbp), %r10
cmp $0, %r10
jl .out_of_bounds
cmp $10, %r10
jge .out_of_bounds
mov -16(%rbp), %r10
mov -32(%rbp), %r11
add %r11, %r10
mov -8(%rbp), %r11
imul $8, %r11
add $a, %r11
mov %r10, 0(%r11)
mov $1, %r10
mov %r10, -24(%rbp)
mov -24(%rbp), %rax
jmp inc_end
jmp .nonreturning_method

inc_end:
leave
ret
main:
enter $704, $0

.main_0:
mov $0, %r10
mov %r10, -8(%rbp)
mov $0, %r10
mov %r10, -16(%rbp)
mov $1, %rdi
mov $0, %rax
call inc
mov %rax, %r10
mov %r10, -24(%rbp)
mov -24(%rbp), %r10
cmp $0, %r10
jl .out_of_bounds
cmp $10, %r10
jge .out_of_bounds
mov $5, %rdi
mov $0, %rax
call inc
mov %rax, %r10
mov %r10, -32(%rbp)
mov -40(%rbp), %r10
mov -32(%rbp), %r11
add %r11, %r10
mov -24(%rbp), %r11
imul $8, %r11
add $a, %r11
mov %r10, 0(%r11)
mov $9, %r10
mov %r10, -8(%rbp)

.main_1:
mov -8(%rbp), %r10
mov $-1, %r11
cmp %r11, %r10
mov $0, %r10
mov $1, %r11
cmovg %r11, %r10
mov %r10, -56(%rbp)
mov -56(%rbp), %r10
cmp $0, %r10
je .main_4

.main_2:
mov -8(%rbp), %r10
cmp $0, %r10
jl .out_of_bounds
cmp $10, %r10
jge .out_of_bounds
mov -8(%rbp), %r10
imul $8, %r10
add $a, %r10
mov 0(%r10), %r10
mov %r10, -48(%rbp)
mov $.main_string_1, %rdi
mov -8(%rbp), %rsi
mov -48(%rbp), %rdx
mov $0, %rax
call printf
mov %rax, %r10

.main_3:
mov -8(%rbp), %r10
mov $1, %r11
sub %r11, %r10
mov %r10, -8(%rbp)
jmp .main_1

.main_4:
jmp main_end

main_end:
mov $0, %rax
leave
ret
.main_string_1:
.string "a[%d] = %d\n"

.out_of_bounds:
mov $1, %eax
mov $-1, %ebx
int $0x80

.nonreturning_method:
mov $1, %eax
mov $-2, %ebx
int $0x80

