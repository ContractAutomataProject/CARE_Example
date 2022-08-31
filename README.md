# Contract Automata Runtime Environment Examples and Evaluation

This repository contains examples of usage of the Contract Automata Runtime Environment (<tt>CARE</tt>).

Check the repository of CARE for more info
https://github.com/ContractAutomataProject/CARE


## Alice and Bob

This is a simple example used for evaluating the Contract Automata Runtime Environment (<tt>CARE</tt>).

Two implementations of the example are available for this example, and are located in <tt>src/main/java/io/github/contractautomata/care/examples/alicebob</tt>. 
The  contract automata are under the folder <tt>/resources/alicebob</tt>.

The implementation  <tt>src/main/java/io/github/contractautomata/care/examples/alicebob/example</tt> uses <tt>CARE</tt>. 
Here the main application is located in the file <tt>AppWithCARE.java</tt> whilst the services are under the folder <tt>principals</tt>.

The second implementation<tt>src/main/java/io/github/contractautomata/care/examples/alicebob/exampleWithoutCare</tt> does not use <tt>CARE</tt>. 
In this case all the low-level communications between services and the orchestrator have been implemented from scratch.

These two implementations are compared to show the benefits brought by using <tt>CARE</tt>. 

Using <tt>CARE</tt>, the measures are: 
Lines of Code = 153,
Cyclomatic Complexity = 16,
Cognitive Complexity = 8.

Without using <tt>CARE</tt>, the measures are:
Lines of Code = 784,
Cyclomatic Complexity = 134,
Cognitive Complexity = 166.

This comparison has been performed using SonarCloud, and is available for inspection at:

<a href="https://sonarcloud.io/component_measures?metric=complexity&selected=contractautomataproject_CARE_Example%3Asrc%2Fmain%2Fjava%2Fio%2Fgithub%2Fcontractautomata%2Fcare%2Fexamples%2Falicebob&id=contractautomataproject_CARE_Example">https://sonarcloud.io/component_measures?metric=complexity&selected=contractautomataproject_CARE_Example%3Asrc%2Fmain%2Fjava%2Fio%2Fgithub%2Fcontractautomata%2Fcare%2Fexamples%2Falicebob&id=contractautomataproject_CARE_Example</a>.



#### Video Tutorial

An  earlier video tutorial for importing and executing the Alice and Bob example with CARE, also showing other features of CARE is available at https://youtu.be/Zq0KVUs9FqM.


## Composition Service

This example is located under the folder
<tt>src/main/java/io/github/contractautomata/care/examples/compositionService/</tt> whilst the corresponding 
contract automata are under the folder <tt>resources/alicebob</tt>.
 
