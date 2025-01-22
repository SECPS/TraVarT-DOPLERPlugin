
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
The Transformations use a rule-based approach.

### UVL feature model to Dopler decision model
The transformation from UVL to Dopler is a two-step process:
1. Decisions are created from the feature tree
2. Rules are created from the conditions and then distributed to the Decisions.

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
> Let $b_1$, $b_2$, ..., $b_m$ be non boolean features (e.g. string features) and children of $G$.\
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
> Let $b_1$, $b_2$, ..., $b_m$ be non boolean features (e.g. string features) and children of $G$.\
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
> When $C$ has an $∧$ as the top element, then split $C$ and create the two constraints $C_1$ and $C_2$.\
> E.g. consider the constraint $A∧¬B$. It will be split up into $A$ and $¬B$.

> ### Rule 1.3.2  Literal Constraint with Optional Feature
> Let $C$ be a contraint.\
> When $C$ is a literal that corresponds to an optional feature $a$, then a single rule is created:
> ````
> if (true) {a = true;}
> ````
> The rule will be stored in $rules(a)$.

> ### Rule 1.3.3  Literal Constraint with Alternative and OR Feature
> Let $C$ be a contraint.\
> When $C$ is a literal that corresponds to an alternative or an or feature $a$, and $p$ is the parent feature of $a$, then a single rule is created:
> ````
> if (true) {p = a;}
> ````
> The rule will be stored in $rules(a)$.

> ### Rule 1.3.3  Complex Contraints
> Let $C$ be a contraint.\
> When $C$ is not a literal and has no $∧$ as root, then $C$ is converted into DNF.
> 
> Let $n$ be the number of conjunctions ($n⩾2$, because $C$ is not a literal and has no $∧$ as root).\
> Let $m_i$ be the number of literals in the $i$-th conjunction.\
> Let $x_{ij}$ be the $j$-th literal in th $i$-th conjunction.
> 
> Then the DNF has the form:\
> $\bigvee_{0<i⩽n} \bigwedge_{0<j⩽m_i} (\neg) x_{ij}$
> 
> One implication constraint will be generated from the DNF, where the first $n-1$ conjunctions create the predicate and the last conjunction creates the conclusion.\
> 
> The implication constraint has the form:\
> $¬(\bigvee_{0<i⩽n-1} \bigwedge_{0<j⩽m_i} (\neg) x_{ij})→ (\bigwedge_{0<j⩽m_n} (\neg) x_{nj})$
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
> The DNF of the given constraint is $(¬A) ∨ (¬C ∧ ¬D) ∨(D ∧ G ∧ H) ∨ (C ∧ G ∧ H) ∨ (¬B)$.\
> And the implication constraint is $\neg((¬A) ∨ (¬C ∧ ¬D) ∨(D ∧ G ∧ H) ∨ (C ∧ G ∧ H)) → (¬B)$.\
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

> ### Rule 1.3.4 Attributes

### Dopler decision model to UVL feature model

