#include <cstdio>


int f(int arr[])
{
    if (arr[2] == 0)
    {
        return 0;
    }

    arr[2] = arr[2] - 1;

    int a = arr[0];
    int b = arr[1];
    int c = f(arr) + b * (a + b * (a + b * (a + b * (a + b * (a + b * (a + b * (a + b * (a + b))))))));

    return c;
}

int main()
{
    int arr[5] = { 1, 2, 3, 4, 5 };
    int a = 1;
    int b = 2;

    int c = f(arr) + b * (a + b * (a + b * (a + b * (a + b * (a + b * (a + b * (a + b * (a + b))))))));

	printf("%d\n", c);

    return 0;
}