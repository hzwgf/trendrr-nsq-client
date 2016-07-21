package com.youzan.nsq.client;

/**
 * Created by wgf on 16/4/23.
 */
public class PiTest {


    public static void main(String[] args){
        System.out.println("start ...");

        long start=System.currentTimeMillis();
        double pi=0.0;


        for(long i=1;i<1000000000;i++){
            pi +=Math.pow(-1,i-1)*(1.0/(2*i-1));
//            System.out.println(pi*4);
        }

        System.out.println(pi*4);

        System.out.println("use time:"+(System.currentTimeMillis()-start)+"ms");



    }




}
