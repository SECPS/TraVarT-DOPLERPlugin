# DOPLERPlugin
The DOPLERPlugin is a plugin for the [TraVarT](https://github.com/SECPS/TraVarT) ecosystem.\
It enables the conversion of UVL feature models to Dopler decision models and vice verse.

## Getting Started
The project is built with Maven and uses JDK 23.\
If you want to use or work on the project, simply clone it with git and import it as a maven project in the IDE of your choice.
To check the installation, run `maven verify` in the project root. The installation is working, when all tests (\~300) run and pass.

The entry point into the code is the class `DoplerPlugin`. From there you get access to the `Serialiser`, `Deserialiser`, `Transformer` and `PrettyPrinter`
- The `Transformer` is responsible for transforming an UVL feature model to a Dopler decision model
- The `Serialiser` is responsible for writing a Dopler decision model into a string
- The `Deserialiser` is responsible for deserializing a string and creating a Dopler decision model with it
- The `PrettyPrinter` is responsible for transforming a Dopler decision model into different representations (e.g. a table)

## Transformations
The `Transformer` functionality is divided into two parts:
1. A Dopler to UVL part that converts a Dopler decision model to an UVL feature model
2. An UVL to Dopler part that converts an UVL feature model to a Dopler decision model

There are also two different strategies for the transformation:
1. ONEWAY - With this strategy all information is translated from one model to another that makes sense in the target model (e.g. mandatory features in UVL are not translated, because there is no decision to make)
2. ROUNDTRIP - With this strategy as much information as possible is kept (the target model will contain redundant and superfluous elements)  

In the following sections the two parts, including an outline of the transformation, are described.\
The Transformations are described in a rule-based approach.

### UVL feature model to Dopler decision model
The transformation from UVL to Dopler is a two-step process:
1. Decisions are created from the feature tree
2. Rules are created from the conditions and then distributed to the Decisions
The rules for the transformations are the following:


> ### Rule 1.1: Optional Group
> Let $G$ be an optional group.\
> Let $a_1$, $a_2$, ..., $a_n$ be children of $G$.\
> Then for every $a_i$ one boolean decision is created.\
> The decisions look like this: 
>|ID|Question|Type|Range|Cardinality|Constraint/Rule|Visible/relevant if  
>|  --------  |  -------  |  -------  |  -------  |  -------  |  -------  |  -------  |
>|$a_1$|$a_1$?|Boolean|false \| true||$rules(a_1)$|$visibility(a_1)$
>|$a_2$|$a_2$?|Boolean|false \| true||$rules(a_2)$|$visibility(a_2)$
>|...|...|...|...|||...
>|$a_n$|$a_n$?|Boolean|false \| true||$rules(a_n)$|$visibility(a_n)$

> ### Rule 1.2: Alternative Group
> Let $G$ be an alternative group.\
> Let $a$ be the parent feature of $G$.\
> Let $a_1$, $a_2$, ..., $a_n$ be children of $G$.\
> Then one enumeration decision is created.\
> The decision looks like this:
>|ID|Question|Type|Range|Cardinality|Constraint/Rule|Visible/relevant if  
>|  --------  |  -------  |  -------  |  -------  |  -------  |  -------  |  -------  |
>|$a$|Which $a$?|Enumeration|$a_1$ \| $a_2$ \|  ... \| $a_n$|1:1|$rules(a)$|$visibility(a)$

> ### Rule 1.3: Or Group
> Let $G$ be an alternative group.\
> Let $a$ be the parent feature of $G$.\
> Let $a_1$, $a_2$, ..., $a_n$ be children of $G$.\
> Then one enumeration decision is created.\
> The decision looks like this:
>|ID|Question|Type|Range|Cardinality|Constraint/Rule|Visible/relevant if  
>|  --------  |  -------  |  -------  |  -------  |  -------  |  -------  |  -------  |
>|$a$|Which $a$?|Enumeration|$a_1$ \| $a_2$ \|  ... \| $a_n$|1:n|$rules(a)$|$visibility(a)$

> ### Rule 1.4: Mandatory Group (only roundtrip)
> Let $G$ be an mandatory group.\
> Let $a_1$, $a_2$, ..., $a_n$ be children of $G$.\
> Then for every $a_i$ one enum decision is created.\
> The decisions look like this:
>|ID|Question|Type|Range|Cardinality|Constraint/Rule|Visible/relevant if  
>|  --------  |  -------  |  -------  |  -------  |  -------  |  -------  |  -------  | 
>|$a_1$\#|Which a1?|Enumeration|$a_1$|1:1|$rules(a_1)$|$visibility(a_1)$
>|$a_2$\#|Which a2?|Enumeration|$a_2$|1:1|$rules(a_2)$|$visibility(a_2)$
>|...|...|...|...|||...
>|$a_n$\#|Which a3?|Enumeration|$a_n$|1:1|$rules(a_n)$|$visibility(a_n)$

### Dopler decision model to UVL feature model
