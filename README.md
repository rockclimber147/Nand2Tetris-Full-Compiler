# Nand2Tetris-Full-Compiler
## Description
This project merges the assembler, VMTranslator, and compiler projects from the NAnd2Tetris course (https://www.nand2tetris.org/course). It is used to compile the high level, object oriented Jack language to stack oriented VM code then Assembly and finally raw binary. It also outputs the optional files, namely the xml files from module 10 and a listing file for the assembler.

## Usage
Place a project folder containing one or more valid .jack files in the Input folder and run the compiler.


The output folder will be populated with the resulting files, each in their respective directories.

## EXAMPLE RUN
Input folder contains Test.jack, an example class showcasing some simple and arbitrary features of Jack code to be compiled.

### Test.jack
```
class Test {
    /* Declare field variables */
    field int x;
    field int y;

    constructor Test new(int Ax, int Ay, int Az) {

        /* set x to an arbitrary expression */
        let x = (3 * (2 - Ax)) + Az;

        /* set y to the second argument */
        let y = Ay;

        /* constructors must return the instance they constructed */
        return this;
    }

    /* returns the sum of the field variables */
    method int getSum(){
        return x + y;
    }

    /* Sets the x field variable */
    method void setX(int newX){
        let x = newX;
        return;
    }
}

```
Test.jack is initially compiled to Test.vm and placed in the Output/ProjectName/vm directory. Vm language instructs a virtual stack machine to carry out the operations defined at the higher level. It makes use of several virtual memory segments, namely LOCAL, ARGUMENT, THIS, THAT, and POINTER. 

LOCAL stores local variables in a function. ARGUMENT stores the incoming argument variables. THIS stores the current object. THAT is used for operations involving another object or array. 

I've added clarifying comments, separated by '|', as well as stack visualizations denoted by '>'


### Test.vm
```
/* Declare field variables */           | Comment output by the compiler automatically to keep track of the current symbols in the table
/*                                      |
FIELD  int x -> this     0              |
FIELD  int y -> this     1              |
*/                                      |
/* set x to an arbitrary expression */ 
    
function Test.new 0                     | Declaring a function
/*
ARG    int Ax -> argument 0
ARG    int Ay -> argument 1
ARG    int Az -> argument 2
*/
    push constant 2                     | Pushes 2 to the stack and
    call Memory.alloc 1                 | makes room for the two FIELD variables for the Test object instance
    pop pointer 0                       | Pointer 0 refers to the THIS segment
    push constant 3                     |> 3,
    push constant 2                     |> 3, 2
    push argument 0                     |> 3, 2, Ax
    sub                                 |> 3, (2-Ax)
    call Math.multiply 2                |> (3*(2-Ax))
    push argument 2                     |> (3*(2-Axx)), Az
    add                                 |> ((3*(2-Axx))+Az)
    pop this 0
/* set y to the second argument */
    push argument 1
    pop this 1
/* constructors must return the instance they constructed */
    push pointer 0
    return
/* returns the sum of the field variables */
    
function Test.getSum 0
/*
ARG    Test this -> argument 0
*/
    push argument 0                    | Method calls always have the first argument being a reference to the object they are acting on.
    pop pointer 0
    push this 0                        |> this.x
    push this 1                        |> this.x, this.y
    add                                |> (this.x+this.y)
    return
/* Sets the x field variable */
    
function Test.setX 0
/*
ARG    Test this -> argument 0
ARG    int  newX -> argument 1
*/
    push argument 0
    pop pointer 0
    push argument 1
    pop this 0
    push constant 0                   | Void returning functions always return 0. It is up to the caller to dispose of this value
    return
// end
```


Assembly files are output from the generated vm files to the Output/ProjectName/asm directory.

Assembly describes the CPU interactions that manipulate the RAM to realize the vm instructions. The CPU has two 16-bit registers, Address (A) and Data (D). Thhe CPU also has access to the register in RAM whose address is stored in the A register (RAM[A}).
Each line of assembly can be seen as a symbolic representation of thre binary that will be read as input.


The RAM follows the following mapping:

RAM 0 - 16: Pointers to virtual segments and temporary variables

RAM 17 - 255: Static Variables

RAM 256 - 2047: The stack

RAM 2047 - 16483: The heap

RAM 16484 - 24575: Screen display and Input

The output .asm file is over 500 lines long. For further information on how this is carried out, please see the attached notes files or visit https://www.nand2tetris.org/course.
