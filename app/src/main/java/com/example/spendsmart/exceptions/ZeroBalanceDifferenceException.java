package com.example.spendsmart.exceptions;

public class ZeroBalanceDifferenceException extends Exception
{
    public ZeroBalanceDifferenceException(String text)
    {
        super(text);
    }
}
