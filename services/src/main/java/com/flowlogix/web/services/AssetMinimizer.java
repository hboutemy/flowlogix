/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowlogix.web.services;

import org.apache.tapestry5.Asset;

/**
 * remove whitespaces, etc. from scripts/CSS
 * 
 * @author lprimak
 */
public interface AssetMinimizer
{
    String minimize(Asset asset);
}
