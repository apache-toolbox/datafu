/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package datafu.test.pig.stats.entropy;

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.pig.data.Tuple;
import org.apache.pig.pigunit.PigTest;
import org.testng.annotations.Test;

import datafu.test.pig.PigTests;

/*
 * R's entropy library: http://cran.r-project.org/web/packages/entropy/entropy.pdf
 * used as our test benchmark 
 */
public class EmpiricalCountEntropyTests extends AbstractEntropyTests
{
  /**

  define Entropy datafu.pig.stats.entropy.EmpiricalCountEntropy();
  
  data = load 'input' as (val:double);
  --describe data;
  data_grouped = GROUP data BY val;
  data_cnt = FOREACH data_grouped GENERATE COUNT(data) AS cnt;
  data_cnt_grouped = GROUP data_cnt ALL;
  data_out = FOREACH data_cnt_grouped GENERATE Entropy(data_cnt);
  store data_out into 'output';
   */
  @Multiline private String entropy;
  
  @Test
  public void uniqValEntropyTest() throws Exception
  {
    PigTest test = createPigTestFromString(entropy);
    
    writeLinesToFile("input",
                     "98.94791",
                     "38.61010",
                     "97.10575",
                     "62.28313",
                     "38.83960",
                     "32.05370",
                     "96.10962",
                     "28.72388",
                     "96.65888",
                     "20.41135");
        
    test.runScript();
    
    /* Add expected values, computed using R:
     * 
     * e.g.
     * 
     * > v=c(98.94791,38.61010,97.10575,62.28313,38.83960,32.05370,96.10962,28.72388,96.65888,20.41135) 
     * > table(v)
     * v
     * 20.41135 28.72388  32.0537  38.6101  38.8396 62.28313 96.10962 96.65888 97.10575 98.94791 
     * 1        1        1        1        1        1        1        1        1        1 
     * > count=c(1,1,1,1,1,1,1,1,1,1)
     * > library(entropy)
     * > entropy(count)
     * [1] 2.302585
     * 
     */
    List<Double> expectedOutput = new ArrayList<Double>();
    expectedOutput.add(2.302585);
    List<Tuple> output = this.getLinesForAlias(test, "data_out");
    verifyEqualEntropyOutput(expectedOutput, output, 5); 
  }

  @Test
  public void singleValEntropyTest() throws Exception
  {
    PigTest test = createPigTestFromString(entropy);
    
    writeLinesToFile("input",
                     "98.94791",
                     "98.94791",
                     "98.94791",
                     "98.94791",
                     "98.94791",
                     "98.94791",
                     "98.94791",
                     "98.94791",
                     "98.94791",
                     "98.94791");
        
    test.runScript();
    
    /* Add expected values, computed using R:
     * 
     * e.g.
     * 
     * > v=c(98.94791,98.94791,98.94791,98.94791,98.94791,98.94791,98.94791,98.94791,98.94791,98.94791) 
     * > table(v)
     * v
     * 98.94791 
     * 10 
     * > count=(10)
     * > library(entropy)
     * > entropy(count)
     * [1] 0
     * 
     */
    List<Double> expectedOutput = new ArrayList<Double>();
    expectedOutput.add(0.0);
    
    List<Tuple> output = this.getLinesForAlias(test, "data_out");
    verifyEqualEntropyOutput(expectedOutput, output, 5); 
  }

  @Test
  public void dupValEntropyTest() throws Exception
  {
    PigTest test = createPigTestFromString(entropy);
    
    writeLinesToFile("input",
                     "98.94791",
                     "38.61010",
                     "97.10575",
                     "62.28313",
                     "38.61010",
                     "32.05370",
                     "96.10962",
                     "38.61010",
                     "96.10962",
                     "20.41135");
        
    test.runScript();
    
    /* Add expected values, computed using R:
     * 
     * e.g.
     * 
     * > v=c(98.94791,38.61010,97.10575,62.28313,38.61010,32.05370,96.10962,38.61010,96.10962,20.41135) 
     * > table(v)
     * v
     * 20.41135  32.0537  38.6101 62.28313 96.10962 97.10575 98.94791 
     * 1        1        3        1        2        1        1 
     * > count=c(1,1,3,1,2,1,1)
     * > library(entropy)
     * > entropy(count)
     * [1] 1.834372
     * 
     */
    List<Double> expectedOutput = new ArrayList<Double>();
    expectedOutput.add(1.834372);
    
    List<Tuple> output = this.getLinesForAlias(test, "data_out");
    verifyEqualEntropyOutput(expectedOutput, output, 5); 
  }

  @Test
  public void emptyInputBagEntropyTest() throws Exception
  {
    PigTest test = createPigTestFromString(entropy);
    
    writeLinesToFile("input"
                     );

    test.runScript();
    
    /* Add expected values, computed using R:
     * 
     * e.g.
     * 
     * > v=c() 
     * > table(v)
     * < table of extent 0 > 
     * > count=c()
     * > library(entropy)
     * > entropy(count)
     * [1] 0 
     * 
     */
    List<Double> expectedOutput = new ArrayList<Double>();
    
    List<Tuple> output = this.getLinesForAlias(test, "data_out");
    verifyEqualEntropyOutput(expectedOutput, output, 5); 
  }

  @Test
  public void singleElemInputBagEntropyTest() throws Exception
  {
    PigTest test = createPigTestFromString(entropy);
    
    writeLinesToFile("input",
                     "98.94791");

    test.runScript();
    
    /* Add expected values, computed using R:
     * 
     * e.g.
     * 
     * > count=c(1)
     * > library(entropy)
     * > entropy(count)
     * [1] 0
     * 
     */
    List<Double> expectedOutput = new ArrayList<Double>();
    expectedOutput.add(0.0);
    
    List<Tuple> output = this.getLinesForAlias(test, "data_out");
    verifyEqualEntropyOutput(expectedOutput, output, 5); 
  }

  /**

  define Entropy datafu.pig.stats.entropy.EmpiricalCountEntropy('$base');

  data = load 'input' as (x:chararray, y:double);
  --describe data;
  data_grouped = GROUP data BY (x, y);
  data_cnt = FOREACH data_grouped GENERATE COUNT(data);
  data_cnt_grouped = GROUP data_cnt ALL;
  data_out = FOREACH data_cnt_grouped GENERATE Entropy(data_cnt);
  store data_out into 'output';
   */
  @Multiline private String pairLogEntropy;
 
  @Test
  public void dupPairValEntropyTest() throws Exception
  {
    PigTest test = createPigTestFromString(pairLogEntropy, "base=log");
    
    writeLinesToFile("input",
                     "hadoop	98.94791",
                     "bigdata	38.61010",
                     "hadoop	97.10575",
                     "datafu	32.05370",
                     "bigdata	38.61010",
                     "datafu	32.05370",
                     "datafu	32.05370",
                     "hadoop	38.61010",
                     "pig	96.10962",
                     "pig	20.41135");
        
    test.runScript();
    
    /* Add expected values, computed using R:
     * 
     * e.g.
     * > t <- data.table(x=c("hadoop","bigdata","hadoop","datafu","bigdata","datafu","datafu","hadoop","pig","pig"),y=c(98.94791,38.61010,97.10575,32.05370,38.61010,32.05370,32.05370,38.61010,96.10962,20.41135))
     * > t <- t[order(x,y)]
     * > count<-c(2,3,1,1,1,1,1)
     * > library(entropy)
     * > entropy(count)
     * [1] 1.834372 
     * 
     */
    List<Double> expectedOutput = new ArrayList<Double>();
    expectedOutput.add(1.834372);
    
    List<Tuple> output = this.getLinesForAlias(test, "data_out");
    verifyEqualEntropyOutput(expectedOutput, output, 5); 
  }

  /**

  define Entropy datafu.pig.stats.entropy.EmpiricalCountEntropy('$base');
  
  data = load 'input' as (val:double);
  --describe data;
  data_grouped = GROUP data BY val;
  data_cnt = FOREACH data_grouped GENERATE COUNT(data) AS cnt;
  data_cnt_grouped = GROUP data_cnt ALL;
  data_out = FOREACH data_cnt_grouped GENERATE Entropy(data_cnt);
  store data_out into 'output';
   */
  @Multiline private String logEntropy;
 
  @Test
  public void dupValEntropyLog2Test() throws Exception
  {
    PigTest test = createPigTestFromString(logEntropy, "base=log2");
    
    writeLinesToFile("input",
                     "98.94791",
                     "38.61010",
                     "97.10575",
                     "62.28313",
                     "38.61010",
                     "32.05370",
                     "96.10962",
                     "38.61010",
                     "96.10962",
                     "20.41135");
        
    test.runScript();
    
    /* Add expected values, computed using R:
     * 
     * e.g.
     * 
     * > v=c(98.94791,38.61010,97.10575,62.28313,38.61010,32.05370,96.10962,38.61010,96.10962,20.41135) 
     * > table(v)
     * v
     * 20.41135  32.0537  38.6101 62.28313 96.10962 97.10575 98.94791 
     * 1        1        3        1        2        1        1 
     * > count=c(1,1,3,1,2,1,1)
     * > library(entropy)
     * > entropy(count, count/sum(count), c("ML"),c("log2"))
     * [1] 2.646439
     * 
     */
    List<Double> expectedOutput = new ArrayList<Double>();
    expectedOutput.add(2.646439);
    
    List<Tuple> output = this.getLinesForAlias(test, "data_out");
    verifyEqualEntropyOutput(expectedOutput, output, 5); 
  }

  @Test
  public void dupValEntropyLog10Test() throws Exception
  {
    PigTest test = createPigTestFromString(logEntropy, "base=log10");
    
    writeLinesToFile("input",
                     "98.94791",
                     "38.61010",
                     "97.10575",
                     "62.28313",
                     "38.61010",
                     "32.05370",
                     "96.10962",
                     "38.61010",
                     "96.10962",
                     "20.41135");
        
    test.runScript();
    
    /* Add expected values, computed using R:
     * 
     * e.g.
     * 
     * > v=c(98.94791,38.61010,97.10575,62.28313,38.61010,32.05370,96.10962,38.61010,96.10962,20.41135) 
     * > table(v)
     * v
     * 20.41135  32.0537  38.6101 62.28313 96.10962 97.10575 98.94791 
     * 1        1        3        1        2        1        1 
     * > count=c(1,1,3,1,2,1,1)
     * > library(entropy)
     * > entropy(count, count/sum(count), c("ML"),c("log10"))
     * [1] 0.7966576
     * 
     */
    List<Double> expectedOutput = new ArrayList<Double>();
    expectedOutput.add(0.7966576);
    
    List<Tuple> output = this.getLinesForAlias(test, "data_out");
    verifyEqualEntropyOutput(expectedOutput, output, 5); 
  }


  /**

  define Entropy datafu.pig.stats.entropy.EmpiricalCountEntropy();
  
  data_cnt = load 'input' as (val:int);
  --describe data_cnt;
  data_cnt_grouped = GROUP data_cnt ALL;
  data_out = FOREACH data_cnt_grouped GENERATE Entropy(data_cnt);
  store data_out into 'output';
   */
  @Multiline private String rawValidInputEntropy;
 
  @Test
  public void rawValidInputEntropyTest() throws Exception
  {
    PigTest test = createPigTestFromString(rawValidInputEntropy); 
    
    writeLinesToFile("input",
                     "0",
                     "38",
                     "0",
                     "62",
                     "38",
                     "32",
                     "96",
                     "38",
                     "96",
                     "0");
        
    test.runScript();
    
    /* Add expected values, computed using R:
     * 
     * e.g.
     * 
     * > count=c(0, 38, 0, 62, 38, 32, 96, 38, 96, 0)
     * > library(entropy)
     * > entropy(count) 
     * [1] 1.846901 
     * 
     */
    List<Double> expectedOutput = new ArrayList<Double>();
    expectedOutput.add(1.846901);
    
    List<Tuple> output = this.getLinesForAlias(test, "data_out");
    verifyEqualEntropyOutput(expectedOutput, output, 5); 
  }

  /**

  define Entropy datafu.pig.stats.entropy.EmpiricalCountEntropy();
  
  data_cnt = load 'input' as (val:double);
  --describe data_cnt;
  data_cnt_grouped = GROUP data_cnt ALL;
  data_out = FOREACH data_cnt_grouped GENERATE Entropy(data_cnt);
  store data_out into 'output';
   */
  @Multiline private String rawInvalidTypeInputEntropy;
 
  @Test
  public void rawInvalidTypeInputEntropyTest() throws Exception
  {
    PigTest test = createPigTestFromString(rawInvalidTypeInputEntropy); 
    
    writeLinesToFile("input",
                     "0.0",
                     "38.0",
                     "0.0",
                     "62.0",
                     "38.0",
                     "32.001",
                     "96.002",
                     "38.01",
                     "96.00001",
                     "0.0");
     try {
         test.runScript();
         List<Tuple> output = this.getLinesForAlias(test, "data_out");
         fail( "Testcase should fail");    
     } catch (Exception ex) {
         assertTrue(ex.getMessage().indexOf("Expect the type of the input tuple to be of ([int, long]), but instead found double") >= 0);
     }
  }

  @Test
  public void rawInValidInputValueEntropyTest() throws Exception
  {
    PigTest test = createPigTestFromString(rawValidInputEntropy); 
    
    writeLinesToFile("input",
                     "0",
                     "-38",
                     "0",
                     "62",
                     "38",
                     "32",
                     "96",
                     "38",
                     "96",
                     "0");
    /* Add expected values, computed using R:
     * 
     * e.g.
     * 
     * > count=c(0, -38, 0, 62, 38, 32, 96, 38, 96, 0)
     * > library(entropy)
     * > entropy(ifelse(count>0,count,0))
     * [1] 1.693862 
     * 
     */
    List<Double> expectedOutput = new ArrayList<Double>();
    expectedOutput.add(1.693862);
    
    List<Tuple> output = this.getLinesForAlias(test, "data_out");
    verifyEqualEntropyOutput(expectedOutput, output, 5); 

  }

  /**

  define Entropy datafu.pig.stats.entropy.EmpiricalCountEntropy();
  
  data_cnt = load 'input' as (f1:chararray, f2:chararray);
  --describe data_cnt;
  data_cnt_grouped = GROUP data_cnt ALL;
  data_out = FOREACH data_cnt_grouped GENERATE Entropy(data_cnt);
  store data_out into 'output';
   */
  @Multiline private String invalidInputSchemaEntropy;
 
  @Test
  public void invalidInputSchemaEntropyTest() throws Exception
  {
    PigTest test = createPigTestFromString(invalidInputSchemaEntropy); 
    
    writeLinesToFile("input",
                     "hadoop	98.94791",
                     "bigdata	38.61010",
                     "hadoop	97.10575",
                     "datafu	32.05370",
                     "bigdata	38.61010",
                     "datafu	32.05370",
                     "datafu	32.05370");
        
    try {
         test.runScript();
         List<Tuple> output = this.getLinesForAlias(test, "data_out");
         fail( "Testcase should fail");    
    } catch (Exception ex) {
         assertTrue(ex.getMessage().indexOf("The field schema of the input tuple is null or its size is not 1") >= 0);
    }
  }

  /**

  define Entropy datafu.pig.stats.entropy.EmpiricalCountEntropy();
  
  data = load 'input' as (val:double);
  --describe data;
  data_grouped = GROUP data BY val;
  data_cnt = FOREACH data_grouped GENERATE COUNT(data) AS cnt;
  data_cnt_grouped = GROUP data_cnt ALL;
  data_out = FOREACH data_cnt_grouped  {
                          data_cnt_ordered = order data_cnt by *;
                          GENERATE Entropy(data_cnt_ordered);
                          }
  store data_out into 'output';
   */
  @Multiline private String accumulatedEntropy;

  @Test
  public void accumulatedEntropyTest() throws Exception
  {
    PigTest test = createPigTestFromString(accumulatedEntropy); 
    
    writeLinesToFile("input",
                     "98.94791",
                     "38.61010",
                     "97.10575",
                     "62.28313",
                     "38.61010",
                     "32.05370",
                     "96.10962",
                     "38.61010",
                     "96.10962",
                     "20.41135");
        
    test.runScript();
    
    List<Double> expectedOutput = new ArrayList<Double>();
    //the same output as @test dupValEntropyTest
    expectedOutput.add(1.834372);
    
    List<Tuple> output = this.getLinesForAlias(test, "data_out");
    verifyEqualEntropyOutput(expectedOutput, output, 5); 
  }
}
