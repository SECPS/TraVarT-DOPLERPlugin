
# DOPLERPlugin
The DOPLERPlugin is a plugin for the [TraVarT](https://github.com/SECPS/TraVarT) ecosystem.
It enables the conversion of UVL feature models to Dopler decision models.

## Getting Started
The project is built with Maven and uses JDK 23.\
If you want to use or work on the project, simply clone it with git and import it as a maven project in the IDE of your choice.
To check the installation, run `maven verify` in the project root. The installation is working, when all tests (\~300) run and pass.

The entry point into the code is the class `DoplerPlugin`. From there you get access to the `Serialiser`, `Deserialiser`, `Transformer` and `PrettyPrinter`
- The `Transformer` is responsible for transforming an UVL feature model to a Dopler decision model
- The `Serialiser` is responsible for writing a Dopler decision model into a string
- The `Deserialiser` is responsible for deserializing a string and creating a Dopler decision model with it
- The `PrettyPrinter` is responsible for transforming a Dopler decision model into different representations like a table


## Transformations
The `Transformer` functionality is divided into two parts:
1. A Dopler to UVL part that converts a Dopler decision model to an UVL feature model
2. An UVL to Dopler part that converts an UVL feature model to a Dopler decision model

There are also two different strategies for the transformation:
1. ONEWAY - With this strategy all information is translated from one model to another that makes sense in the target model (e.g. mandatory features in UVL are not translated because there is no decision to make)
2. ROUNDTRIP - With this strategy as much information as possible is kept (the target model will contain redundant and superfluous elements)  

In the following sections the two parts, including an outline of the transformation, are described.\
The transformations are rule-based.

### Not working
There are quite a few constructs that cannot be transformed from UVL to Dopler and from Dopler to UVL.\
This is a non-complete list of all these constructs.\
From UVL to Dopler:
- Constraints over feature attributes with standard arithmetic operations (e.g.: +, -, *, /, =, !=, >, <)
- Type Numeric-Constraints (e.g.: sum or avg)
- Numeric operations  (e.g.: floor and ceil)
- Type String-Constraints (e.g.: len or comparisons of string features)

From Dopler to UVL:
- Complex visibility constraints
- Some action types (e.g.: StringEnforce or NumberEnforce)
- Some condtion types (e.g.: DoubleLiteralExpression or  IsTaken)

### Transformation from UVL feature model to Dopler decision model
The transformation from UVL to Dopler is a three-step process:
1. Decisions are created from the feature tree
2. Rules are created from the conditions and then distributed to the decisions
3. More decisions and rules are created from attributes (only roundtrip)

The rules for the transformations are the following:


> ### Rule 1.1.1: Optional Group
> Let $G$ be an optional group.\
> Let $a_1$, $a_2$, ..., $a_n$ be boolean features and children of $G$.\
> Let $b_1$, $b_2$, ..., $b_m$ be non boolean features (e.g. string features) and children of $G$.\
> Then for every $a_i$ one boolean decision and for every $b_i$ one boolean and one type decision is created:
>|ID|Question|Type|Range|Cardinality|Constraint/Rule|Visible/relevant if  
>|  --------  |  -------  |  -------  |  -------  |  -------  |  -------  |  -------  |
>|$a_1$|$a_1$?|Boolean|false \| true||$rules(a_1)$|$visibility(a_1)$
>|$a_2$|$a_2$?|Boolean|false \| true||$rules(a_2)$|$visibility(a_2)$
>|...|...|...|...|||...
>|$a_n$|$a_n$?|Boolean|false \| true||$rules(a_n)$|$visibility(a_n)$
>|$b_1$|What $b_1$?|$type(b_1)$|||$rules(b_1)$|$b_1Check$  
>|$b_1Check$|$b_1$?|Boolean|false \| true|||$visibility(b_1)$
>|$b_2$|What $b_2$?|$type(b_2)$|||$rules(b_2)$|$b_2Check$ 
>|$b_2Check$|$b_2$?|Boolean|false \| true|||$visibility(b_2)$
>|...|...|...|...|||...
>|$b_m$|What $b_m$?|$type(b_m)$|||$rules(b_m)$|$b_mCheck$
>|$b_mCheck$|$b_m$?|Boolean|false \| true|||$visibility(b_m)$

> ### Rule 1.1.2: Alternative Group
> Let $G$ be an alternative group.\
> Let $a$ be the parent feature of $G$.\
> Let $a_1$, $a_2$, ..., $a_n$ be boolean features and children of $G$.\
> Let $b_1$, $b_2$, ..., $b_m$ be non boolean features (e.g. string features) and children of $G$.\
> Then one enumeration decision and $m$ type decisions are created:
>|ID|Question|Type|Range|Cardinality|Constraint/Rule|Visible/relevant if  
>|  --------  |  -------  |  -------  |  -------  |  -------  |  -------  |  -------  |
>|$a$|Which $a$?|Enumeration|$a_1$ \| $a_2$ \|  ... \| $a_n$ \| $b_1$ \| $b_2$ \|  ... \| $b_m$|1 : 1|$rules(a)$,<br> $rules(a_i)$,<br> $rules(b_i)$|$visibility(a)$
>|$b_1*$|What $b_1$?|$type(b_1)$||||$a.b_1$  
>|$b_2*$|What $b_2$?|$type(b_2)$||||$a.b_2$
>|...|...|...|...|||...
>|$b_m*$|What $b_m$?|$type(b_m)$||||$a.b_m$

> ### Rule 1.1.3: Or Group
> Let $G$ be an alternative group.\
> Let $a$ be the parent feature of $G$.\
> Let $a_1$, $a_2$, ..., $a_n$ be boolean features and children of $G$.\
> Let $b_1$, $b_2$, ..., $b_m$ be non boolean features (e.g. string features) and children of $G$.\
> Then one enumeration decision and $m$ type decisions are created:
>|ID|Question|Type|Range|Cardinality|Constraint/Rule|Visible/relevant if  
>|  --------  |  -------  |  -------  |  -------  |  -------  |  -------  |  -------  |
>|$a$|Which $a$?|Enumeration|$a_1$ \| $a_2$ \|  ... \| $a_n$ \| $b_1$ \| $b_2$ \|  ... \| $b_m$|1 : $n+m$|$rules(a)$,<br> $rules(a_i)$,<br> $rules(b_i)$|$visibility(a)$
>|$b_1*$|What $b_1$?|$type(b_1)$||||$a.b_1$  
>|$b_2*$|What $b_2$?|$type(b_2)$||||$a.b_2$
>|...|...|...||||...
>|$b_m*$|What $b_m$?|$type(b_m)$||||$a.b_m$

> ### Rule 1.1.4: Mandatory Group (one way)
> Let $G$ be a mandatory group.\
> Let $a_1$, $a_2$, ..., $a_n$ be  boolean features and children of $G$.\
> Let $b_1$, $b_2$, ..., $b_m$ be non-boolean features (e.g. string features) and children of $G$.\
> Then for every $b_i$ one type decision is created:
>|ID|Question|Type|Range|Cardinality|Constraint/Rule|Visible/relevant if  
>|  --------  |  -------  |  -------  |  -------  |  -------  |  -------  |  -------  | 
>|$b_1$|What $b_1$?|$type(b_1)$|||$rules(b_1)$|$visibility(b_1)$  
>|$b_2$|What $b_2$?|$type(b_2)$|||$rules(b_2)$|$visibility(b_2)$  
>|...|...|...|||...|...
>|$b_m$|What $b_m$?|$type(b_m)$|||$rules(b_m)$|$visibility(b_m)$  

> ### Rule 1.1.5: Mandatory Group (roundtrip)
> Let $G$ be a mandatory group.\
> Let $a_1$, $a_2$, ..., $a_n$ be  boolean features and children of $G$.\
> Let $b_1$, $b_2$, ..., $b_m$ be non-boolean features (e.g. string features) and children of $G$.\
> Then for every $a_i$ one enumeration decision and for every $b_i$ one type decision is created:
>|ID|Question|Type|Range|Cardinality|Constraint/Rule|Visible/relevant if  
>|  --------  |  -------  |  -------  |  -------  |  -------  |  -------  |  -------  | 
>|$a_1$\#|Which a1?|Enumeration|$a_1$|1 : 1|$rules(a_1)$|$visibility(a_1)$
>|$a_2$\#|Which a2?|Enumeration|$a_2$|1 : 1|$rules(a_2)$|$visibility(a_2)$
>|...|...|...|...|...|...|...
>|$a_n$\#|Which a3?|Enumeration|$a_n$|1 : 1|$rules(a_n)$|$visibility(a_n)$
>|$b_1$|What $b_1$?|$type(b_1)$|||$rules(b_1)$|$visibility(b_1)$  
>|$b_2$|What $b_2$?|$type(b_2)$|||$rules(b_2)$|$visibility(b_2)$  
>|...|...|...|||...|...
>|$b_m$|What $b_m$?|$type(b_m)$|||$rules(b_m)$|$visibility(b_m)$ 

> ### Rule 1.2.1 Visibility (one way)
> Let $a$ be a feature.\
> Then $visibility(a)$ resolves to the first non-mandatory parent of $a$.\
> If there is no non-mandatory parent, then $visibility(a)$ resolves to $true$.\
> E.g. consider this feature model:
> ````
> features  
>     a  
>         optional  
>             b  
>                 mandatory  
>                     c  
>                         optional  
>                             d
> ````
> Then the decision model looks like this:
>|ID|Question|Type|Range|Cardinality|Constraint/Rule|Visible/relevant if  
>|  --------  |  -------  |  -------  |  -------  |  -------  |  -------  |  -------  | 
>|b|b?|Boolean|false \| true|||true  
>|d|d?|Boolean|false \| true|||b

>### Rule 1.2.1 Visibility (roundtrip)
> Let $a$ be a feature.\
> Then $visibility(a)$ resolves to the parent of $a$.\
> If there is no parent, then $visibility(a)$ resolves to $true$.\
> E.g. consider this feature model:
> ````
> features  
>     a  
>         optional  
>             b  
>                 mandatory  
>                     c  
>                         optional  
>                             d
> ````
> Then the decision model looks like this:
>|ID|Question|Type|Range|Cardinality|Constraint/Rule|Visible/relevant if  
>|  --------  |  -------  |  -------  |  -------  |  -------  |  -------  |  -------  | 
>|a#|Which a?|Enumeration|a|1 : 1||true  
>|b|b?|Boolean|false \| true|||a#.a  
>|c#|Which c?|Enumeration|c|1 : 1||b  
>|d|d?|Boolean|false \| true|||c#.c

> ### Rule 1.3.1 And Constraint
> Let $C$ be a contraint.\
> When $C$ has an $`\&`$ as the top element, then split $C$ and create the two constraints $C_1$ and $C_2$.\
> E.g. consider the constraint $A`\&`!B$. It will be split up into $A$ and $!B$.

> ### Rule 1.3.2 Literal Constraint of Optional Feature
> Let $C$ be a contraint.\
> When $C$ is a literal that corresponds to an optional feature $a$, then a single rule is created:
> ````
> if (true) {a = true;}
> ````
> The rule will be stored in $rules(a)$.

> ### Rule 1.3.3  Literal Constraint of Alternative or OR Feature
> Let $C$ be a contraint.\
> When $C$ is a literal that corresponds to an alternative or an or feature $a$, and $p$ is the parent feature of $a$, then a single rule is created:
> ````
> if (true) {p = a;}
> ````
> The rule will be stored in $rules(a)$.

> ### Rule 1.3.4  Literal Constraint of Mandatory Feature
> Let $C$ be a contraint.\
> When $C$ is a literal that corresponds to a mandatory feature $a$, and $p$ is the first non-mandtory parent feature of $a$, then $a$ is replaced with $p$.\
> When there is no non-mandatory parent, then $C$ is replaced with $true$.

> ### Rule 1.3.5 Complex Contraint
> Let $C$ be a contraint.\
> When $C$ is not a literal and has no $`\&`$ as root, then $C$ is converted into DNF.
> 
> Let $n$ be the number of conjunctions ($n⩾2$, because $C$ is not a literal and has no $`\&`$ as root).\
> Let $m_i$ be the number of literals in the $i$-th conjunction.\
> Let $x_{ij}$ be the $j$-th literal in th $i$-th conjunction.
> 
> Then the DNF has the form:\
> $\bigvee_{0<i⩽n} \bigwedge_{0<j⩽m_i} (\neg) x_{ij}$
> 
> One implication constraint will be generated from the DNF, where the first $n-1$ conjunctions create the predicate and the last conjunction creates the conclusion.
> 
> The implication constraint has the form:\
> $\neg(\bigvee_{0<i⩽n-1} \bigwedge_{0<j⩽m_i} (\neg) x_{ij})→ (\bigwedge_{0<j⩽m_n} (\neg) x_{nj})$
> 
> This implication constraint will then be converted into a rule and stored in $rules(x_{n1})$.
> 
> E.g. consider the following feature model:
> ````
> features  
>    root  
>        alternative  
>            A  
>            B  
>            C  
>            D  
>        optional  
>            E  
>            F  
>            G  
>            H  
> 
> constraints  
>    (A & B & (C|D)) => ((D | C) & (G & H))
> ````
> The DNF of the given constraint is $(!A) | (!C `\&` !D) |(D `\&` G `\&` H) | (C `\&` G `\&` H) | (!B)$.\
> And the implication constraint is $\neg((!A) | (!C `\&` !D) | (D `\&` G `\&` H) | (C `\&` G `\&` H)) => (!B)$.\
> From the predicate an action is created. In this case:
> ````
> {disAllow(root.B);}
> ````
> From the conclusion a condition is created. In this case:
> ````
> (!(((!root.A || (!root.C && !root.D)) || ((root.D && G) && H)) || ((root.C && G) && H)))
> ````
> The rule is then given to $rules(B)$.
> 
> The complete decision model would look like this:
> |ID|Question|Type|Range|Cardinality|Constraint/Rule|Visible/relevant if  
>|  --------  |  -------  |  -------  |  -------  |  -------  |  -------  |  -------  | 
>|E|E?|Boolean|false \| true|||true  
>|F|F?|Boolean|false \| true|||true  
>|G|G?|Boolean|false \| true|||true  
>|H|H?|Boolean|false \| true|||true  
>|root|Which root?|Enumeration|A \| B \| C \| D|1:1|"if (!(((!root.A \|\| (!root.C && !root.D)) \|\| ((root.D && G) && H)) \|\| ((root.C && G) && H))) {disAllow(root.B);}"|true

> ### Rule 1.4.1 Attribute Decision (roundtrip)
> Let $f$ be a feature.\
> Let $f$ have an attribute $a$ with the name $name(a)$, the value $value(a)$ and the type $type(value(a))$.
> 
> Depending on $type(value(a))$ a string, double or boolean decision is created:
> |ID|Question|Type|Range|Cardinality|Constraint/Rule|Visible/relevant if  
> |  --------  |  -------  |  -------  |  -------  |  -------  |  -------  |  -------  | 
> |a#name(a)#Attribute|$name(a)$?|Boolean|false \| true|||false  
> |a#name(a)#Attribute|How much $name(a)$?|Double||||false  
> |a#name(a)#Attribute|What $name(a)$?|String||||false
> 
>Because the Dopler decision model does not support integer values, integer attributes create double decisions.

> ### Rule 1.4.2 Attribute Rule (roundtrip)
> Let $f$ be a feature.\
> Let $g$ be the parent group of $f$.\
> Let $p$ be the parent feature of $g$.\
> Let $f$ have an attribute $a$ with the name $name(a)$ and the value $value(a)$.\
> Then one rule will be created and put into $rules(f)$.\
> Depending on $g$ the created rule looks different.
> 
> $g$ is optional:
> ````
> if (a) {a#name(a)#Attribute = value(a);}
> ````
> 
> $g$ is or, alternative or mandatory:
> ````
> if (p.a) {a#name(a)#Attribute = value(a);}
> ````

### Dopler decision model to UVL feature model
The transformation from Dopler to UVL is a three-step process:
1. The feature model tree is created from the decisions and their visibilities
2. The feature model tree is beautified
3. Rules are converted into constraints and added to the feature model

The rules for the transformations are the following:
> ### Rule 2.1.1 Insert Standard Root
> A feature with the name "STANDARD_MODEL_NAME" is always created and used as the root of the feature model.\
> The minimum feature model is therefore:
> ````
> features  
>     STANDARD_MODEL_NAME
> ````

> ### Rule 2.1.2 Boolean Decision
> Let $d$ be a boolean decision.\
> Then one optional group $g$ is created with one child feature $d$.\
> The parent of $g$ is $parent(d)$.\
> The subtree would look like this:
> ````
> parent(d)
>     optional
>         d
> ````

> ### Rule 2.1.3 Enumeration Decision
> Let $d$ be an enumeration decision.\
> Let $a_1$, $a_2$, ..., $a_n$ be the range of $d$.\
> Let $max$ be the maximal cardinalityof $d$.\
> When $max=1$, then one alternative group $g$ is created with the child features $a_1$, $a_2$, ..., $a_n$.\
> When $max≠1$, then one or group $g$ is created with the child features $a_1$, $a_2$, ..., $a_n$.\
> The parent of $g$ is $parent(d)$.\
> The subtree would look like this:
> ````
> parent(d)
>     alternative/or
>         a_1
>         a_2
>         ...
>         a_n
> ````

> ### Rule 2.1.4 Double Decision
> Let $d$ be a double decision.\
> Then one mandatory group $g$ is created with one child feature $d$ of the type real.\
> The parent of $g$ is $parent(d)$.\
> The subtree would look like this:
> ````
> parent(d)
>     mandatory
>         Real d
> ````

> ### Rule 2.1.5 String Decision
> Let $d$ be a string decision.\
> Then one mandatory group $g$ is created with one child feature $d$ of the type string.\
> The parent of $g$ is $parent(d)$.\
> The subtree would look like this:
> ````
> parent(d)
>     mandatory
>         String d
> ````

> ### Rule 2.2.1 Visibility is $true$
> Let $d$ be a decision.\
> Let $v$ be the visibility of $d$.\
> When $v$ is $true$, then $parent(d)$ resolves to the root of the feature model.\
> E.g. the decision model:
> |ID|Question|Type|Range|Cardinality|Constraint/Rule|Visible/relevant if  
> |  --------  |  -------  |  -------  |  -------  |  -------  |  -------  |  -------  | 
> |D|D?|Boolean|true \| false|||true
> F|Which F?|Enumeration|A \| B \| C|1:1||true
> 
> is converted into:
> ````
>features  
>        STANDARD_MODEL_NAME  
>                alternative  
>                        A
>                        B
>                        C
>                optional  
>                        D
> ````

> ### Rule 2.2.2 Visibility is Value of Enumeration Decision
> Let $d$ be a decision.\
> Let $e$ be an enumreation decision.\
> Let $a$ be part of the range of $e$.\
> Let $v$ be the visibility of $d$.\
> When $v$ has the form $e.a$, then $parent(d)$ resolves to $a$.\
> E.g. the decision model:
> |ID|Question|Type|Range|Cardinality|Constraint/Rule|Visible/relevant if  
> |  --------  |  -------  |  -------  |  -------  |  -------  |  -------  |  -------  | 
> |D|D?|Boolean|true \| false|||F.A  
> |F|Which F?|Enumeration|A \| B \| C|1:1||true
> 
> is converted into:
> ````
> features  
>     STANDARD_MODEL_NAME  
>         alternative  
>             A  
>                 optional  
>                     D  
>             B  
>             C
> ````

> ### Rule 2.2.3 Visibility is Non-Enumeration Decision
> Let $d$ be a decision.\
> Let $e$ be a non-enumeration decision.\
> Let $v$ be the visibility of $d$.\
> When $v$ has the form $e$, then $parent(d)$ resolves to $e$.\
> E.g. the decision model:
> |ID|Question|Type|Range|Cardinality|Constraint/Rule|Visible/relevant if  
> |  --------  |  -------  |  -------  |  -------  |  -------  |  -------  |  -------  | 
> |D|D?|Boolean|true \| false|||true  
> |F|Which F?|Enumeration|A \| B \| C|1:1||D
> 
> is converted into:
> ````
> features  
>     STANDARD_MODEL_NAME  
>         optional  
>              D  
>                   alternative  
>                       A  
>                       B  
>                       C
> ````

> ### Rule 2.3.1 Combine Optional and Mandatory Groups
> Let $g_1$, $g_2$, ..., $g_n$ be all optional or all mandatory groups.\
> When $g_1$, $g_2$, ..., $g_n$ share the same parent feature, then the groups are all combined into one group $g$.\
> $g$ has all children of $g_1$, $g_2$, ..., $g_n$.\
> E.g. the model:
> ````
>features  
>    root
>        mandatory  
>            X
>        mandatory  
>            Y 
>        optional  
>            A  
>        optional  
>            B  
>        optional  
>            C
> ```` 
> will be converted into:
> ````
>features  
>    root
>        mandatory  
>            X
>            Y 
>        optional  
>            A  
>            B  
>            C
> ````

> ### Rule 2.3.2 Replace single Alternative Group with Mandatory Group (one way)
> Let $g$ be an alternative group.\
> When $g$ only has one child, then $g$ is replaced with the mandatory group $g'$\
> E.g. the model:
> ````
>features  
>    Sandwich  
>        alternative  
>            Bread
> ```` 
> will be converted into:
> ````
>features  
>    Sandwich  
>        mandatory
>            Bread
> ````

> ### Rule 2.3.3 Simplify Type Feature (one way)
> Let $f$ be a feature.\
> When $f$ has a single child group $g$, $g$ is mandatory and $g$ has a single type feature $t$ then replace $f$ with $t$.\
> E.g. the model:
> ````
> features  
>    root
>        optional
>            Plane
>                mandatory  
>                    String Name
> ```` 
> will be converted into:
> ````
> features  
>    root
>        optional
>            String Name
> ````

> ### Rule 2.3.3 Remove Standard Root
> The standard root $r$ of the model is removed, when $r$ has one child group $g$, $g$ is mandatory and $g$ has only one child feature.\
> E.g. the model:
> ````
> features  
>     STANDARD_MODEL_NAME  
>         mandatory  
>             A
>                 or
>                     B
>                     C
>                 optional
>                     D
>                     E
> ```` 
> will be converted into:
> ````
> features  
>     A
>         or
>             B
>             C
>         optional
>             D
>             E
> ````

> ### Rule 2.4.1 Handle Action
> Let $A$ be an action.\
> $A$ can have one of three forms:
> 1. enumeration enforce: $d = a_i$, where $d$ is a enumeration decision with the value $a_i$
> 2. disllow - $disallow(d.a_i)$, where $d$ is a enumeration decision with the value $a_i$
> 3. boolean enforce: $d = true$, where $d$ is a boolean decision
> 4. boolean enforce: $d = false$, where $d$ is a boolean decision
> 
> Independent of the concrete from, extactly one constraint will be generated for every actio:
> 1. $d = a_i$ becomes $a_1$
> 2. $disallow(d.a_i)$ becomes $!a_i$
> 3. $d = true$ becomes $e$
> 4. $d = false$ becomes $!e$

> ### Rule 2.4.2 Handle Condition
> Let $C$ be a condition.\
> With $C$ will one constraint in the feature model be genereted.\
> Let $transformed(C)$ be this constraint.\
> The pattern matching rules for the transformation are the following:
> 1. $C$ is $getValue(d) = true$, where $d$ is a boolean decision, then return $d$ 
> 2. $C$ is $getValue(d) = false$, where $d$ is a boolean decision, then return $!d$ 
> 3. $C$ is $getValue(d) = a_i$, where $d$ is an enumeration decision and $a_i$  one of its values, then return $a_i$ 
> 4. $C$ is $true$, then return nothing
> 5. $C$ is $false$, then produce an error
> 4. $C$ is $!C'$, where $C'$ is another constraint, then return $!transformed(C')$ 
> 5. $C$ is $C_1`\&``\&`C_2$, where $C_1$ and $C_2$ are constraints, then return $transformed(C_1) `\&` transformed(C_2)$ 
> 6. $C$ is $C_1||C_2$, where $C_1$ and $C_2$ are constraints, then return $transformed(C_1) | transformed(C_2)$ 
>
> E.g. the condition:
> ````
> !getValue(z) = a || (getValue(y) = false && getValue(x)=true)
> ````
> is transformed to:
> ````
> !a|(y & x)
> ````


> ### Rule 2.4.6 Complete Rule
> Let $R$ be an rule in the Dopler decision model.\
> $R$ has exactly one condition $C$ and the actions $A_1$, $A_2$, ..., $A_n$.\
> Let $C'$ be the transformed condtion. \
> Let $A_1'$, $A_2'$, ..., $A_n'$ be the transformed actions.\
> Then one implication constraint $C'=>A_1 `\&` A_2'`\&` ... `\&` A_n'$ will be created and added to the feature model.
> E.g. the rule:
> ````
> if ((getValue(Human) = SoftwareEngineer)) {disallow(Hobby.Sports);Hobby = Programming;IsSuperCool = true;}
> ````
> is transformed to
> ````
> SoftwareEngineer=> !Sports & Programming & IsSuperCool
> ````
