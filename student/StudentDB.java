package info.kgeorgiy.ja.shevchenko.student;

import info.kgeorgiy.java.advanced.student.AdvancedQuery;
import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class StudentDB implements AdvancedQuery {
    // :NOTE: static
    private static final String DEFAULT_STRING = "";

    private static final Student DEFAULT_STUDENT = new Student(0, DEFAULT_STRING, DEFAULT_STRING, GroupName.M3237);

    private static final Map.Entry<GroupName, List<Student>> DEFAULT_GROUP = new AbstractMap.SimpleEntry<>(null, Collections.emptyList());

    private static final Comparator<Student> ALPHABET_STUDENTS_NAME_ORDER = Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .reversed()
            .thenComparing(Comparator.naturalOrder());

    private static final Function<Student, String> FULL_NAME_MAPPER = student -> student.getFirstName() + " " + student.getLastName();

    @Override
    // Returns student groups, where both groups and students within a group are ordered by name.
    public List<Group> getGroupsByName(Collection<Student> collection) {
        // :NOTE: toList
        // не нашла такого метода
        return getGroupsWithSortedStudentsStream(collection, ALPHABET_STUDENTS_NAME_ORDER).collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> collection) {
        return getGroupsWithSortedStudentsStream(collection, Comparator.naturalOrder()).collect(Collectors.toList());
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> collection) {
        return getLargestGroup(collection, Comparator
                .comparingInt((Map.Entry<GroupName, List<Student>> p) -> p.getValue().size())
                .thenComparing(Map.Entry::getKey));
    }

    @Override
    /*
     * Returns group containing maximum number of students with distinct first names.
     * If there are more than one largest group, the one with smallest name is returned.
     */
    public GroupName getLargestGroupFirstName(Collection<Student> collection) {
        return getLargestGroup(collection, Comparator
                .comparingInt((Map.Entry<GroupName, List<Student>> p) -> getDistinctFirstNames(p.getValue()).size())
        );
    }

    @Override
    // Returns student first names.
    public List<String> getFirstNames(List<Student> list) {
        return mapAndCollectToList(list, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> list) {
        return mapAndCollectToList(list, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> list) {
        return mapAndCollectToList(list, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> list) {
        return mapAndCollectToList(list, FULL_NAME_MAPPER);
    }

    @Override
    // Returns distinct student first names in lexicographic order.
    public Set<String> getDistinctFirstNames(List<Student> list) {
        return mapAndCollect(list, Student::getFirstName, Collectors.toCollection(TreeSet::new));
    }

    @Override
    // Returns a first name of the student with maximal id.
    public String getMaxStudentFirstName(List<Student> list) {
        return list.stream().max(Comparator.naturalOrder()).map(Student::getFirstName).orElse(DEFAULT_STRING);
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> collection) {
        return makeSortedListOfStudentsWithComparator(collection, Comparator.naturalOrder());
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> collection) {
        return makeSortedListOfStudentsWithComparator(collection, ALPHABET_STUDENTS_NAME_ORDER);
    }

    @Override
    // Returns students having specified first name. Students are ordered by name.
    public List<Student> findStudentsByFirstName(Collection<Student> collection, String s) {
        return filterAndSortStudentsToList(collection, student -> student.getFirstName().equals(s));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> collection, String s) {
        return filterAndSortStudentsToList(collection, student -> student.getLastName().equals(s));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> collection, GroupName groupName) {
        return filterAndSortStudentsToList(collection, student -> student.getGroup().name().equals(groupName.name()));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> collection, GroupName groupName) {
        return filterAndSortStudents(collection,
                student -> student.getGroup().name().equals(groupName.name()),
                Collectors.toMap(Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }

    private List<Student> makeSortedListOfStudentsWithComparator(Collection<Student> collection, Comparator<? super Student> comparator) {
        return collection.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    //get grouped by name of group stream
    private Stream<Map.Entry<GroupName, List<Student>>> getGroupAndStudentsStream(Stream<Student> studentStream) {
        return studentStream
                .collect(Collectors.groupingBy(Student::getGroup,
                        TreeMap::new, Collectors.toList()))
                .entrySet()
                .stream();
    }

    //get stream ofo group with sorted students
    // :NOTE: можно передавать Collection
    private Stream<Group> getGroupsWithSortedStudentsStream(Collection<Student> students, Comparator<? super Student> comparator) {
        return getGroupAndStudentsStream(students.stream()).map(
                elem -> new Group(elem.getKey(),
                        elem.getValue()
                                .stream()
                                .sorted(comparator)
                                .collect(Collectors.toList()))
        );
    }

    private <S, T extends Collection<S>> T
    mapAndCollect(Collection<Student> collection,
                  Function<? super Student, ? extends S> mapper,
                  Collector<? super S, ?, T> collector) {
        return collection.stream()
                .map(mapper)
                .collect(collector);
    }

    private <S> List<S>
    mapAndCollectToList(Collection<Student> collection,
                        Function<? super Student, ? extends S> mapper) {
        return mapAndCollect(collection, mapper, Collectors.toList());
    }

    private GroupName getLargestGroup(Collection<Student> collection, Comparator<Map.Entry<GroupName, List<Student>>> cmp) {
        Map.Entry<GroupName, List<Student>> elem = getGroupAndStudentsStream(collection.stream())
                .max(cmp)
                .orElse(DEFAULT_GROUP);
        return elem.getKey();
    }

    private <T> T
    filterAndSortStudents(Collection<Student> collection,
                          Predicate<? super Student> predicate,
                          Collector<? super Student, ?, T> collector) {
        return collection.stream().filter(predicate).sorted(ALPHABET_STUDENTS_NAME_ORDER).collect(collector);
    }

    private List<Student> filterAndSortStudentsToList(Collection<Student> collection,
                                                      Predicate<? super Student> predicate) {
        return filterAndSortStudents(collection, predicate, Collectors.toList());
    }

    @Override
    public String getMostPopularName(Collection<Student> students) {
        Map.Entry<String, List<Student>> elem = students.stream()
                .collect(Collectors
                        .groupingBy(Student::getFirstName,
                                Collectors.toList()))
                .entrySet()
                .stream()
                .max(Comparator
                        .comparingInt((Map.Entry<String, List<Student>> p) -> p.getValue().size())
                        .thenComparing(Map.Entry::getKey))
                .orElse(null);
        return elem == null ? DEFAULT_STRING : elem.getKey();
    }

    private <S> List<S> filterStudentsByIndices(
            Collection<Student> students,
            int[] indices,
            Function<? super Student, ? extends S> mapper) {
        List<Student> temp = (List<Student>) students;
        return mapAndCollectToList(Arrays.stream(indices).mapToObj(temp::get)
                .collect(Collectors.toList()), mapper);
    }

    @Override
    public List<String> getFirstNames(Collection<Student> students, int[] indices) {
        return filterStudentsByIndices(students, indices, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(Collection<Student> students, int[] indices) {
        return filterStudentsByIndices(students, indices, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(Collection<Student> students, int[] indices) {
        return filterStudentsByIndices(students, indices, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(Collection<Student> students, int[] indices) {
        return filterStudentsByIndices(students, indices, FULL_NAME_MAPPER);
    }
}
