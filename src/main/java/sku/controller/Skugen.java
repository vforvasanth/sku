package sku.controller;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;

import bean.Sku;
import sku.service.BigQueryOperations;

@Controller
public class Skugen {
    
    @Autowired
    private BigQueryOperations bigQueryOperations;
    
    Map<String, List<Sku>> inMemCache = new HashMap<>();
    DecimalFormat decimalFormat = new DecimalFormat("0.000000");
    
    @GetMapping("/getJson")
    public String getJson(@RequestParam(name = "sku", required = true, defaultValue = "") String sku, Model model) {

        long startTime = System.nanoTime();
        
        Sku skuObj = new Sku();
        skuObj.item = "Sample Item";
        skuObj.sku = sku;
        skuObj.detail1 = "SG";
        skuObj.detail2 = "Fiber";
        skuObj.detail3 = "Black";
        skuObj.store = "Clementi";
        skuObj.price = 55.00;
        skuObj.stock = 6;

        long endTime = System.nanoTime();

        model.addAttribute("sku", sku);
        model.addAttribute("skus", new Gson().toJson(skuObj));
        model.addAttribute("timeTaken", decimalFormat.format((double)(endTime - startTime)/(1000000000)));
        return "json";
    }

    @GetMapping("/updateJson")
    public String updateJson(@RequestParam(name = "sku", required = true, defaultValue = "") String sku, Model model) {
        
        long startTime = System.nanoTime();

        List<Sku> skus = new ArrayList<>();
        if (inMemCache.containsKey(sku)) {
            skus = inMemCache.get(sku);
        } else {
            skus = bigQueryOperations.lookup(sku);
            inMemCache.put(sku, skus);
        }

        long endTime = System.nanoTime();
        
        model.addAttribute("sku", sku);
        model.addAttribute("skus", new Gson().toJson(CollectionUtils.isEmpty(skus) ? "No donut" : skus));
        model.addAttribute("timeTaken", decimalFormat.format((double)(endTime - startTime)/(1000000000)));
        return "json";
    }
}
