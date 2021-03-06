����   2 b  0com/polyspot/iw/ies/process/URLAccessCreatorTest  java/lang/Object creator "Lcom/polyspot/iw/ies/api/Enricher; document )Lcom/polyspot/iw/business/model/Document; <init> ()V Code
   	 
  ,com/polyspot/iw/ies/process/URLAccessCreator
  	     LineNumberTable LocalVariableTable this 2Lcom/polyspot/iw/ies/process/URLAccessCreatorTest; !createNewFieldAccessUrlIfNotExist 
Exceptions  java/lang/Exception RuntimeVisibleAnnotations Lorg/junit/Test;  /com/polyspot/iw/business/model/Document$Builder
    	 ! (Z)V # /home/polyspot
  % & ' url E(Ljava/lang/String;)Lcom/polyspot/iw/business/model/Document$Builder;
  ) * + build +()Lcom/polyspot/iw/business/model/Document;	  -   / 1 0  com/polyspot/iw/ies/api/Enricher 2 3 enrich T(Lcom/polyspot/iw/business/model/Document;)Lcom/polyspot/iw/business/model/Document;
 5 7 6 'com/polyspot/iw/business/model/Document 8 9 getAccessUrl ()Ljava/lang/String;
 ; = < org/junit/Assert > ? assertNotNull (Ljava/lang/Object;)V 1doNothingIfUrlFieldNotPresentorValueIsNullOrEmpty B 
_accessUrl
 5 D E F getField :(Ljava/lang/String;)Lcom/polyspot/iw/business/model/Field;
 ; H I ? 
assertNull copyUrlValueInCaseofHttp L http://plbala/blabla.pdf
 ; N O P assertEquals '(Ljava/lang/Object;Ljava/lang/Object;)V copyUrlValueInCaseofHttps S https://plbala/blabla.pdf "buildCorrectAccessUrlFileInWindows V C:\home\polyspot\blabla.pdf X #file:///C:/home/polyspot/blabla.pdf  buildCorrectAccessurlFileInLinux [ /home/polyspot/blabla.pdf ]  file:///home/polyspot/blabla.pdf 
SourceFile URLAccessCreatorTest.java InnerClasses Builder !                  	 
     B     *� *� Y� � �                              
                   f     0*� Y� "� $� (� ,**� *� ,� . � ,*� ,� 4� :�              %  /         0      @ 
                   N     *� Y� � (� ,*� ,A� C� G�                             J 
                   v     <*� Y� K� $� (� ,**� *� ,� . � ,*� ,� 4� :*� ,� 4K� M�           #  $ % % / & ; '        <      Q 
                   v     <*� Y� R� $� (� ,**� *� ,� . � ,*� ,� 4� :*� ,� 4R� M�           +  , % - / . ; /        <      T 
                   v     <*� Y� U� $� (� ,**� *� ,� . � ,*� ,� 4� :*� ,� 4W� M�           3  4 % 5 / 6 ; 7        <      Y 
                   v     <*� Y� Z� $� (� ,**� *� ,� . � ,*� ,� 4� :*� ,� 4\� M�           ;  < % = / > ; ?        <      ^    _ `   
   5 a 	