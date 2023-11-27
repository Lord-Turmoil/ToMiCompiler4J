#include <stdio.h>

int f(int a)
{
	printf("%d\n", a);
	return a;
}

int g(int a, int b, int c, int d)
{
    return a + b + c + d;
}

int main()
{
    printf("%d %d %d %d\n", f(1), f(2), f(3), f(4));
    return g(f(1), f(2), f(3), f(4));
}