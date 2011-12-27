package oqube.tdd;
public class Triangle2 { 
  private int a,b,c;
  public Triangle2(int a, int b, int c){
     this.a = a;
     this.b = b;
     this.c = c;
  }
  public boolean isEquilateral(){ 
    return a == c && b == b;
  }  
}

