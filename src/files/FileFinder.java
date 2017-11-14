package files;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileFinder {

    private Pattern p = null;
    private Matcher m = null;
    private long totalLength = 0;
    private long filesNumber = 0;
    private long directoriesNumber = 0;
    private final int FILES = 0;
    private final int DIRECTORIES = 1;
    private final int ALL = 2;

    public FileFinder() {
    }

    /**
     *
     * Этот метод выполняет поиск всех объектов (файлов и директорий), начиная с
     * заданной директории (startPath)
     *
     * @param startPath Начальная директория поиска
     * @return Список (List) найденных объектов
     * @throws java.lang.Exception если возникли ошибки в процессе поиска
     */
    public List findAll(String startPath) throws Exception {

        return find(startPath, "", ALL);

    }

    /**
     * Этот метод выполняет поиск объектов (файлов и директорий), которые
     * соответствуют заданному регулярному выражению (mask), начиная с заданной
     * директории (startPath)
     *
     * @param startPath Начальная директория поиска
     * @param mask регулярное выражение, которому должны соответствовать имена
     * найденный объектов
     * @throws java.lang.Exception если возникли ошибки в процессе поиска
     * @return Список (List) найденных объектов
     */
    public List findAll(String startPath, String mask)
            throws Exception {

        return find(startPath, mask, ALL);

    }

    /**
     * Этот метод выполняет поиск всех файлов, начиная с заданной директории
     * (startPath)
     *
     * @param startPath Начальная директория поиска
     * @return Список (List) найденных объектов
     * @throws java.lang.Exception если возникли ошибки в процессе поиска
     */
    public List findFiles(String startPath)
            throws Exception {

        return find(startPath, "", FILES);

    }

    /**
     * Этот метод выполняет поиск файлов, которые соответствуют заданному
     * регулярному выражению (mask), начиная с заданной директории (startPath)
     *
     * @param startPath Начальная директория поиска
     * @param mask регулярное выражение, которому должны соответствовать имена
     * найденный объектов
     * @throws java.lang.Exception если возникли ошибки в процессе поиска
     * @return Список (List) найденных объектов
     */
    public List findFiles(String startPath, String mask)
            throws Exception {

        return find(startPath, mask, FILES);

    }

    /**
     * Этот метод выполняет поиск всех директорий (папок), начиная с заданной
     * директории (startPath)
     *
     * @param startPath Начальная директория поиска
     * @return Список (List) найденных объектов
     * @throws java.lang.Exception если возникли ошибки в процессе поиска
     */
    public List findDirectories(String startPath)
            throws Exception {

        return find(startPath, "", DIRECTORIES);

    }

    /**
     * Этот метод выполняет поиск директорий (папок), которые соответствуют
     * заданному регулярному выражению (mask), начиная с заданной директории
     * (startPath)
     *
     * @param startPath Начальная директория поиска
     * @param mask регулярное выражение, которому должны соответствовать имена
     * найденный объектов
     * @throws java.lang.Exception если возникли ошибки в процессе поиска
     * @return Список (List) найденных объектов
     */
    public List findDirectories(String startPath, String mask)
            throws Exception {

        return find(startPath, mask, DIRECTORIES);

    }

    /**
     *
     * Возвращает суммарный размер найденных файлов
     *
     * @return размер найденных файлов (байт)
     */
    public long getDirectorySize() {
        return totalLength;
    }

    /**
     * Возвращает общее количество найденных файлов
     *
     * @return количество найденных файлов
     */
    public long getFilesNumber() {
        return filesNumber;
    }

    /**
     * Возвращает общее количество найденных директорий (папок)
     *
     * @return количество найденных директорий (папок)
     */
    public long getDirectoriesNumber() {
        return directoriesNumber;
    }


    /*
     Проверяет, соответствует ли имя файла заданному
     регулярному выражению. Возвращает true, если найденный
     объект соответствует регулярному выражению, false – в
     противном случае.
     */
    private boolean accept(String name) {


        if (p == null) {

            return true;

        }


        m = p.matcher(name);


        if (m.matches()) {

            return true;

        } else {

            return false;

        }

    }

    /*
     Этот метод выполняет начальные установки поиска.
     Затем вызывает метод search для выполнения поиска.
     */
    private List find(String startPath, String mask, int objectType)
            throws Exception {

        if (startPath == null || mask == null) {

            throw new Exception("Ошибка: не заданы параметры поиска");

        }

        File topDirectory = new File(startPath);

        if (!topDirectory.exists()) {

            throw new Exception("Ошибка: указанный путь не существует");

        }

        if (!mask.equals("")) {

            p = Pattern.compile(mask, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

        }


        filesNumber = 0;

        directoriesNumber = 0;

        totalLength = 0;


        ArrayList res = new ArrayList(100);

        search(topDirectory, res, objectType);

        p = null;


        return res;

    }


    /*
     Этот метод выполняет поиск объектов заданного типа.
     Если, в процессе поиска, встречает вложенную директорию
     (папку), то рекурсивно вызывает сам себя.
     Результаты поиска сохраняются в параметре res.
     Текущая директория – topDirectory.
     Тип объекта (файл или директория) – objectType.
     */
    private void search(File topDirectory, List res, int objectType) {


        File[] list = topDirectory.listFiles();


        for (int i = 0; i < list.length; i++) {


            if (list[i].isDirectory()) {

                if (objectType != FILES && accept(list[i].getName())) {


                    directoriesNumber++;

                    res.add(list[i]);

                }


                search(list[i], res, objectType);

            } else {


                if (objectType != DIRECTORIES && accept(list[i].getName())) {

                    filesNumber++;

                    totalLength += list[i].length();

                    res.add(list[i]);

                }

            }

        }

    }
}
