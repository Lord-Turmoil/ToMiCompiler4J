#include <stdio.h>

int f1(int a, int b) {
    return a + b;
}

int f2(int a, int b) {
    return a * b;
}

int f3(int a) {
    return a * a;
}

int main()
{
    int a = 66;
    int b = 99;
    int c = 77;
    int d = 88;
    int e = 55;

    a = 5 * f1(a, b) / f2(c, d) - f3(e);
    printf("%d\n", a);
    a = 1 - a * f1(a, b) % f3(c);
    printf("%d\n", a);
    return 0;
}
