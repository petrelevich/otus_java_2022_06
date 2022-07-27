package homework;


import java.util.*;

public class CustomerService {

    //todo: 3. надо реализовать методы этого класса
    //важно подобрать подходящую Map-у, посмотрите на редко используемые методы, они тут полезны
    private final TreeMap<Customer, String> map = new TreeMap<>((o1, o2) -> {
        if (o1.getScores() == o2.getScores()) return 0;
        return o1.getScores() > o2.getScores() ? 1 : -1;
    });

    public Map.Entry<Customer, String> getSmallest() {
        //Возможно, чтобы реализовать этот метод, потребуется посмотреть как Map.Entry сделан в jdk
        Map.Entry<Customer, String> smallest = map.firstEntry();
        return Map.entry(new Customer(smallest.getKey()), smallest.getValue());
    }

    public Map.Entry<Customer, String> getNext(Customer customer) {
        Map.Entry<Customer, String> next = map.higherEntry(customer);
        if (next == null) return null;
        return Map.entry(new Customer(next.getKey()), next.getValue());
    }

    public void add(Customer customer, String data) {
        this.map.put(customer, data);
    }
}
