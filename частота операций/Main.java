import java.util.*;
import java.util.concurrent.*;

public class Main {
    // Создаем Map для хранения в ключах попавшиеся частоты буквы 'R',
   // а в значениях — количество раз их появления
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();
    private static final Object lock = new Object(); // Объект  монитор для синхронизации

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        List<String> text = new ArrayList<>(); // Создаем список для хранения рандомно созданных строк
        for (int i = 0; i < 1000; i++) { // Рандомно в цикле создаем строки
            text.add(generateRoute("RLRFR", 100));
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(1000); // Создаем пул потоков в количестве 1000
        List<Future<Integer>> futures = new ArrayList<>(); // Создаем список для заполнения его объектами Future

        // Создаем отдельный поток для вывода лидера в sizeToFreq
        Thread leaderThread = new Thread(() -> {
            while (!Thread.interrupted()) {// пока поток не прерван
                //создадим блок синхронизации ,который обеспечивает безопасный доступ к критической секции,
                // в которой поток leaderThread ожидает сигнала от других потоков
                synchronized (lock) {
                    try {
                        lock.wait(); // Ждем сигнала от заполняющих потоков
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Восстанавливаем флаг прерывания
                        return;
                    }
                    // переменная для хранения максимального размера строки из букв "R"
                    int maxSize = 0;
                    //переменная для хранения максимального количества повторений
                    int maxFreq = 0;

                    // создадим блок синхронизации который обеспечивает безопасный доступ к общему ресурсу sizeToFreq
                    synchronized (sizeToFreq) {
                        //перебираем мапу
                        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
                            if (entry.getValue() > maxFreq) {// если текущее значение больше иаксимального
                                maxFreq = entry.getValue();//то обновляем
                                maxSize = entry.getKey();//значение переменных
                            }
                        }
                        //выводим в консоль максимальное значение
                        System.out.printf("Самое частое количество повторений %d (встретилось %d раз)\n", maxSize, maxFreq);
                        System.out.println("Другие размеры:");
                        //выводим в консоль все значения кроме максимального
                        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
                            if (entry.getKey() != maxSize) {
                                System.out.printf("- %d (%d раз)\n", entry.getKey(), entry.getValue());
                            }
                        }
                    }
                }
            }
        });
        leaderThread.start();

        //проходим по списку из строк
        for (String str : text) {

            //реализуем интерфейс  Callable который находит наибольшую последовательность
            // букв в каждой строке из списка text и обновляет частоту этих последовательностей
            // в HashMap sizeToFreq.
            Callable<Integer> task = () -> {

                //вызываем метод для нахождения максимальной длины строки из повторяющихся букв "R"
                // и сохраняем в переменную
                int max = largestStringOfLettersR(str);

                //блок синхронизации гарантирует, что только один поток может работать с HashMap sizeToFreq
                // в данный момент времени. Внутри блока вызывается метод sizeToFreq
                synchronized (sizeToFreq) {
                    //вызываем метод merge который если такая пара существует увеличивает частоту
                    // найденой последовательности если не существует добовляет пару
                    sizeToFreq.merge(max, 1, Integer::sum);
                } //блок синхронизации используется для отправки сигнала потоку, который выводит лидера
                synchronized (lock) {
                    lock.notify(); // Отправляем сигнал потоку, выводящему лидера
                }
                return max;
            };
            //отправляем задачу в пул потоков и ссылку добавляем в список фьючерсов
            futures.add(threadPool.submit(task));
        }
          //перебираем фьючерсы
        for (Future<Integer> future : futures) {
            future.get();//ожидаем результата выполнения задачи
        }

        threadPool.shutdown();//останавливаем потоки
        leaderThread.interrupt(); // Прерываем поток, который выводит лидера
    }

    //метод для определения максимальной длины строки из "R"
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

    //метод для рандомного создания строки из заданных литеров и определенной длины
    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}