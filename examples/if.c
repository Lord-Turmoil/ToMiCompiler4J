#include <stdio.h>

int main()
{
    int a = 5;

    if (a > 3)
    {
        if (a < 5)
        {
            printf("a = 4\n");
        }
        else
        {
            printf("a >= 5\n");
        }
    }
    else
    {
        printf("a <= 3\n");
    }

    return 0;
}