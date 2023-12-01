#include <stdio.h>

void __vec_mul(int res[], int mat[][2], int vec[]) {
    res[0] = vec[0] * mat[0][0] + vec[1] * mat[0][1];
    res[1] = vec[0] * mat[1][0] + vec[1] * mat[1][1];
    return;
}

int main()
{
    int matrix[2][2] = {{1, 2}, {3, 4}};
    int vector[2] = {5, 6};
    int result[2];

    __vec_mul(result, matrix, vector);
    printf("Result = [%d, %d]\n", result[0], result[1]);

    return 0;
}