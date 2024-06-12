import java.util.*;
import java.util.concurrent.*;

public class Main {
    //создаем Map для хранения в ключах попавшиеся частоты буквы 'R',
    // а в значениях — количество раз их появления
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        List<String> text = new ArrayList<>();//создаем список для хранения рандомно созданных строк
        for (int i = 0; i < 1000; i++) { //радомно в цикле создаем строки
            text.add(generateRoute("RLRFR", 100));
        }
        //объявляем переменную типа ExecutorService для получения доступа
        // к методам интерфейса ExecutorService позволяющие управлять и
        // выполнять задачи в многопоточности
        //вызов метода newFixedThreadPoo создает и возвращает новый экземпляр
        // ExecutorService  с фиксированным пулом потоков.
        ExecutorService threadPool = Executors.newFixedThreadPool(1000);

        //создаем список для заполнения его  объектами Future в дальнейшем используем
        // эти объекты для получения  результатов их работы
        List<Future<Integer>> futures = new ArrayList<>();

        for (String str : text) {
            // Для каждой строки в text создаем Callable задачу, которая вызывает
            // largestStringOfLettersR и обновляет sizeToFreq в синхронизированном блоке.
            Callable<Integer> task = () -> {
                int max = largestStringOfLettersR(str);
                synchronized (sizeToFreq) {  //начало синхронизированного блока
                    if (sizeToFreq.containsKey(max)) { // если такой ключ уже есть
                        sizeToFreq.put(max, sizeToFreq.get(max) + 1);//то  увеличиваем его значение на 1
                    } else {
                        sizeToFreq.put(max, 1);//или добавляем в мапу новые ключ - значение
                    }
                }
                return max;
            };
            futures.add(threadPool.submit(task));//добавляем объект с задачей  Callable в список фьючерсов
        }

        // Ждем завершения всех задач
        for (Future<Integer> future : futures) {
            future.get();
        }

        // Находим максимальное значение в sizeToFreq
        int maxFreq = 0;//Эта переменная будет хранить максимальную частоту встречаемости размеров.
        int maxSize = 0;// Эта переменная будет хранить размер, который встречается чаще всего.
        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {//цикл, который будет итерироваться по всем элементам в sizeToFreq
            if (entry.getValue() > maxFreq) {// если текущая частота  больше, чем максимальная частота
                maxFreq = entry.getValue();//то мы обновляем значение maxFreq на текущую частоту
                maxSize = entry.getKey();// также обновляем значение maxSize на текущий размер
            }
        }

        // Выводим результат
        System.out.printf("Самое частое количество повторений %d (встретилось %d раз)\n", maxSize, maxFreq);
        System.out.println("Другие размеры:");
        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {//перебираем коллекцию
            if (entry.getKey() != maxSize) {//ищем наибольший по значению ключ
                System.out.printf("- %d (%d раз)\n", entry.getKey(), entry.getValue());//выводим в консоль все ключ-значение кроме максимального
            }
        }

        threadPool.shutdown();//останавливаем потоки
    }

    //метод определяющий длину строки состоящую только из бцкв "R"
    public static int largestStringOfLettersR(String text) {
        int maxSize = 0;
        for (int i = 0; i < text.length(); i++) {
            for (int j = 0; j < text.length(); j++) {
                if (i >= j) {
                    continue;
                }
                boolean lfFound = false;
                for (int k = i; k < j; k++) {
                    if (text.charAt(k) == 'L' || text.charAt(k) == 'F') {
                        lfFound = true;
                        break;
                    }
                }
                if (!lfFound && maxSize < j - i) {
                    maxSize = j - i;
                }
            }
        }
        return maxSize;
    }

    //метод рандомно создающий строку из заданных символов и заданной длины
    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}
