from typing import *

import strawberry

@strawberry.type
class User:
    name: str
    age: int
    friends: List[str]

User(name='abc', age=123, friends=['efg','hig'])
User(<warning descr="Expected type 'str', got 'int' instead">name=123</warning>, <warning descr="Expected type 'int', got 'str' instead">age='abc'</warning>, <warning descr="Expected type 'List[str]', got 'List[int]' instead">friends=[1,2,3]</warning>)

