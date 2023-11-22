package tomic.llvm.ir.value;

import tomic.llvm.ir.type.ArrayType;
import tomic.llvm.ir.type.Type;

import java.util.ArrayList;
import java.util.List;

public class ConstantData extends Constant {
    private boolean isAllZero;
    private int value;
    private ArrayList<ConstantData> values;

    public ConstantData(Type type, int value) {
        super(ValueTypes.ConstantDataTy, type);
        this.value = value;
        this.isAllZero = value == 0;
    }

    public ConstantData(List<ConstantData> values) {
        super(ValueTypes.ConstantDataTy, ArrayType.get(values.get(0).getType(), values.size()));
        this.values = new ArrayList<>(values);
        this.isAllZero = true;
        for (var value : values) {
            if (!value.isAllZero) {
                isAllZero = false;
                break;
            }
        }
    }
}
