#include <cstdio>
int fib_matrix[2][2] = {{1, 1}, {1, 0}};
void __mat_mul(int res[][2], int x[][2], int y[][2]) {
    res[0][0] = x[0][0] * y[0][0] + x[0][1] * y[1][0];
    res[0][1] = x[0][0] * y[0][1] + x[0][1] * y[1][1];
    res[1][0] = x[1][0] * y[0][0] + x[1][1] * y[1][0];
    res[1][1] = x[1][0] * y[0][1] + x[1][1] * y[1][1];
    return;
}

int __power(int n, int cur[][2], int res[][2]) {
    if (n == 1) {
        res[0][0] = cur[0][0];
        res[0][1] = cur[0][1];
        res[1][0] = cur[1][0];
        res[1][1] = cur[1][1];
        return 0;
    } else {
        __mat_mul(res, cur, fib_matrix);
        cur[0][0] = res[0][0];
        cur[0][1] = res[0][1];
        cur[1][0] = res[1][0];
        cur[1][1] = res[1][1];
        return __power(n - 1, cur, res);
    }
    return 0;
}

int power(int n, int res[][2]) {
    if (n <= 0)
        return -1;

    int temp[2][2] = {{1, 1}, {1, 0}};

    return __power(n, temp, res);
}

int out(int r[][2]) {
    printf("---\n%d %d\n%d %d\n", r[0][0], r[0][1], r[1][0], r[1][1]);
    return 1;
}

int main() {
    int res[2][2];

    power(1, res);
    out(res);
    power(2, res);
    out(res);
    power(3, res);
    out(res);
    power(4, res);
    out(res);

    return 0;
}
