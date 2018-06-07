package com.mono.common;


import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;

import org.supercsv.util.CsvContext;

public class ParseShort extends CellProcessorAdaptor implements StringCellProcessor {

    public ParseShort() {
        super();
    }

    public Object execute(Object value, CsvContext context) {
        this.validateInputNotNull(value, context);
        Short result;
        if(value instanceof Short) {
            result = (Short)value;
        } else {
            if(!(value instanceof String)) {
                String actualClassName = value.getClass().getName();
                throw new SuperCsvCellProcessorException(String.format("the input value should be of type Short or String but is of type %s", new Object[]{actualClassName}), context, this);
            }

            try {
                result = Short.valueOf((String)value);
            } catch (NumberFormatException var5) {
                throw new SuperCsvCellProcessorException(String.format("\'%s\' could not be parsed as an Short", new Object[]{value}), context, this, var5);
            }
        }

        return this.next.execute(result, context);
    }
}

