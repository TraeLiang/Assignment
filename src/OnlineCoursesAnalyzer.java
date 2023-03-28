import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This is just a demo for you, please run it on JDK17 (some statements may be not allowed in lower version).
 * This is just a demo, and you can extend and implement functions
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
                        Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
                        Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
                        Double.parseDouble(info[12]), Double.parseDouble(info[13]), Double.parseDouble(info[14]),
                        Double.parseDouble(info[15]), Double.parseDouble(info[16]), Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]), Double.parseDouble(info[19]), Double.parseDouble(info[20]),
                        Double.parseDouble(info[21]), Double.parseDouble(info[22]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {
        return courses.stream().collect(Collectors.groupingBy(Course::getInstitution,
                Collectors.summingInt(Course::getParticipants)));
    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> map = new TreeMap<>();
        for (int i = 0; i < courses.size(); i++) {
            String institution = courses.get(i).institution;
            String subject = courses.get(i).subject;
            int participants = courses.get(i).participants;
            String key = institution + "-" + subject;
            if (map.containsKey(key)) {
                map.put(key, map.get(key) + participants);
            } else {
                map.put(key, participants);
            }
        }
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        map.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey())).forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
        return sortedMap;
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Map<String, List<List<String>>> result = new HashMap<>();
        for (Course course : courses) {
            String[] instructors = course.getInstructors().split(", ");
            for (String instructor : instructors) {
                if (!result.containsKey(instructor)) {
                    result.put(instructor, new ArrayList<>(Arrays.asList(new ArrayList<>(), new ArrayList<>())));
                }
                if (instructors.length == 1) {
                    result.get(instructor).get(0).add(course.getCourseTitle());
                } else {
                    result.get(instructor).get(1).add(course.getCourseTitle());
                }
            }
        }
        for (List<List<String>> lists : result.values()) {
            for (List<String> list : lists) {
                List<String> l1 = new ArrayList<>();
                Set<String> s = new HashSet<>();
                for (String ss : list)
                    if (s.add(ss)) l1.add(ss);
                list.clear();
                list.addAll(l1);
                Collections.sort(list);
            }
        }
        return result;
    }

    //4
    public List<String> getCourses(int topK, String by) {
        List<Course> sortedCourses = new ArrayList<>(courses);
        if (by.equals("hours")) {
            sortedCourses.sort((a, b) -> {
                if (b.totalHours == a.totalHours) {
                    return a.title.compareTo(b.title);
                } else {
                    if (b.totalHours > a.totalHours) {
                        return 1;
                    } else return -1;
                }
            });
        } else if (by.equals("participants")) {
            sortedCourses.sort((a, b) -> {
                if (b.getParticipants() == (a.getParticipants())) {
                    return a.title.compareTo(b.title);
                } else {
                    if (b.participants > a.participants) {
                        return 1;
                    } else {return -1;}
                }
            });
        }

        List<String> result = new ArrayList<>();
        for (Course sortedCours : sortedCourses) {
            String courseTitle = sortedCours.title;
            if (!result.contains(courseTitle)) {
                result.add(courseTitle);
            }
        }
        return result.subList(0,topK);

    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        List<String> result = new ArrayList<>();
        for (Course course : courses) {
            if (course.getCourseSubject().toLowerCase().contains(courseSubject.toLowerCase())
                    && course.percentAudited >= percentAudited
                    && course.totalHours <= totalCourseHours) {
                result.add(course.getCourseTitle());
            }
        }
        Collections.sort(result);
        return result.stream().distinct().collect(Collectors.toList());
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        // Calculate average values for each course
        Map<String, Double[]> courseAverages = new HashMap<>();
        for (Course course : courses) {
            String courseNumber = course.number;
            Double[] averages = courseAverages.getOrDefault(courseNumber, new Double[]{0.0, 0.0, 0.0});
            averages[0] += course.medianAge;
            averages[1] += course.medianAge;
            averages[2] += course.percentDegree;
            courseAverages.put(courseNumber, averages);
        }
        for (Map.Entry<String, Double[]> entry : courseAverages.entrySet()) {
            Double[] averages = entry.getValue();
            averages[0] /= courses.size();
            averages[1] /= courses.size();
            averages[2] /= courses.size();
        }

        // Calculate similarity values for each course
        Map<String, Double> similarityValues = new HashMap<>();
        for (Map.Entry<String, Double[]> entry : courseAverages.entrySet()) {
            String courseNumber = entry.getKey();
            Double[] averages = entry.getValue();
            double similarityValue = Math.pow(age - averages[0], 2) + Math.pow(gender * 100 - averages[1], 2) + Math.pow(isBachelorOrHigher * 100 - averages[2], 2);
            similarityValues.put(courseNumber, similarityValue);
        }

        // Sort courses by similarity value and title
        List<Course> sortedCourses = new ArrayList<>(courses);
        sortedCourses.sort((c1, c2) -> {
            int result = similarityValues.get(c1.number).compareTo(similarityValues.get(c2.number));
            if (result == 0) {
                result = c1.getCourseTitle().compareTo(c2.getCourseTitle());
            }
            return result;
        });

        // Return top 10 courses
        List<String> recommendedCourses = new ArrayList<>();
        for (int i = 0; i < 10 && i < sortedCourses.size(); i++) {
            recommendedCourses.add(sortedCourses.get(i).getCourseTitle());
        }
        return recommendedCourses;
    }



}

class Course {
    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }

    public int getParticipants() {
        return participants;
    }

    public String getInstitution() {
        return institution;
    }

    public String getSubject() {
        return subject;
    }

    public String getCourseSubject() {
        return subject;
    }

    public String getInstructors() {
        return instructors;
    }

    public String getCourseTitle() {
        return title;
    }
    public double getAverageMale(String number) {
        return percentMale;
    }

    public double getAverageMedianAge(String number) {
        return medianAge;
    }

    public double getAverageBachelorOrHigher(String number) {
        return percentDegree;
    }
}
