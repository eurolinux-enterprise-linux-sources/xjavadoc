package ab;

/** @no-star at-the-end */
class AB{

  interface C{

     String getFoo();
     void setFoo(String foo);

     String getBar();
     void setBar(int bar);

     boolean[] isThisIsNotAnAccessor();
     void setThisIsAMutator(boolean[] a);
  }
}
interface B{
  class D{}
}