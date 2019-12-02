import java.util.ArrayList;

interface Rule{
    abstract Double evaluate(Double value);
    abstract String getVariableName();
}