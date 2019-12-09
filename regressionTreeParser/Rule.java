import java.util.ArrayList;

interface Rule{
    abstract boolean evaluate(Double value);
    abstract String getVariableName();
}