package com.study.lambdaDemo.classDemo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author:wangyi
 * @Date:2019/11/4
 */
public class LambdaContex {
    public static void main(String[] args) {
//        method1();
//        method2();
        method3();
    }


    static void method1() {
        List<Student> students = new ArrayList<>(3);
        students.add(new Student("路飞", 22, 175));
        students.add(new Student("红发", 40, 180));
        students.add(new Student("白胡子", 50, 185));

        List<Student> list = students.stream()
                .filter(stu -> stu.getStature() < 180)
                .collect(Collectors.toList());
        System.out.println(list);
    }

    static void method2() {
        List<Student> students = new ArrayList<>(3);
        students.add(new Student("路飞", 22, 175));
        students.add(new Student("红发", 40, 180));
        students.add(new Student("白胡子", 50, 185));

        List<String> collect = students.stream().map(student -> student.getName()).collect(Collectors.toList());
        System.out.println(collect);
    }

    static void method3() {
        List<Student> students = new ArrayList<>(3);
        students.add(new Student("路飞", 22, 175));
        students.add(new Student("红发", 40, 180));
        students.add(new Student("白胡子", 50, 185));

//        List<Student> studentList = new ArrayList<>(1);
//        studentList.add(new Student("wangyi", 26, 170));

        List<Student> collect = Stream.of(students,
                Arrays.asList(new Student("艾斯", 25, 183),
                        new Student("雷利", 48, 176)))
                .flatMap(students1 -> students1.stream()).collect(Collectors.toList());

        Stream<List<Student>> students1 = Stream.of(students,
                Arrays.asList(new Student("艾斯", 25, 183),
                        new Student("雷利", 48, 176)));

        List<List<Student>> listList = new ArrayList<>();
        listList.add(students);
        listList.add(new ArrayList<>(Arrays.asList(new Student("艾斯", 25, 183),
                new Student("雷利", 48, 176))));

        Stream<List<Student>> stream = listList.stream();

//        List<Student> collect = students.stream().flatMap(students1 -> studentList.stream()).collect(Collectors.toList());
        System.out.println(collect);
    }
}
