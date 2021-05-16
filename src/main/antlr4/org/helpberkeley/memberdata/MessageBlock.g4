/*
 * Copyright (c) 2020-2021. helpberkeley.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

grammar MessageBlock;

block         : label element* ;

label         : '[' ID ']' ;

element       :
              QUOTED_STRING     # String
              | variable        # Var
              | conditional     # If
              | loop            # LoopElement
              | loop_continue   # ContinueElement
              ;

variable      :
              simple_ref
              | list_ref
              | struct_ref
              ;

simple_ref    : '${' ID '}' ;

list_ref      : '&{' COMPOSITE '}' ;

list_name_ref : '&{' ID '}' ;


struct_ref    : '${' COMPOSITE '}' ;

conditional   :
              'IF' expr 'THEN' '{' element+ '}'          # ConditionalTrue
              | 'IF' 'NOT' expr 'THEN' '{' element+ '}'  # ConditionalFalse
              ;

expr          :
              simple_ref
              | struct_ref
              | list_ref
              ;

loop          :
              'LOOP' list_name_ref '{' element+ '}' # LoopList
              | 'LOOP' list_ref '{' element+ '}'    # LoopListMember
              ;

loop_continue : 'CONTINUE' ;

ID            : LETTER (LETTER|DIGIT)* ;
COMPOSITE     : ID '.' ID ;
LETTER        : [a-zA-Z] ;
DIGIT         : [0-9] ;
WHITESPACE    : [ \t\r\n]+ -> skip ;
QUOTED_STRING : '"' (~'"')* '"' ;
CODE_ESCAPE   : '```' -> skip ;
LINE_COMMENT  : '//' .*? '\r'? '\n' -> skip ;
