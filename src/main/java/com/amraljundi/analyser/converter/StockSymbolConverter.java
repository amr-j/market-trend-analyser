package com.amraljundi.analyser.converter;

import com.amraljundi.analyser.model.StockSymbol;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StockSymbolConverter implements Converter<String, StockSymbol> {

    @Override
    public StockSymbol convert(String source) {
        return new StockSymbol(source);
    }
}
