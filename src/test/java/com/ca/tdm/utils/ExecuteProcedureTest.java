package com.ca.tdm.utils;

import org.junit.Assert;
import org.junit.Test;

public class ExecuteProcedureTest {
    @Test
    void test() {
        ExecuteProcedure tst = new ExecuteProcedure();
        Object newProposta;

        newProposta = tst.mask("1234", "SYBASE","sa/interOP@123@10.162.27.147:5000:master", "23", "NovaProposta");

        Assert.assertTrue(newProposta.toString().matches("\\d+"));

        System.out.println(newProposta);
    }
}
