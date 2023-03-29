import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


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
                Double.parseDouble(info[12]), Double.parseDouble(info[13]),
                Double.parseDouble(info[14]), Double.parseDouble(info[15]),
                Double.parseDouble(info[16]), Double.parseDouble(info[17]),
                Double.parseDouble(info[18]), Double.parseDouble(info[19]),
                Double.parseDouble(info[20]), Double.parseDouble(info[21]),
                Double.parseDouble(info[22]));
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
    map.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                    .thenComparing(Map.Entry.comparingByKey()))
            .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
    return sortedMap;
  }

  //3
  public Map<String, List<List<String>>> getCourseListOfInstructor() {
    Map<String, List<List<String>>> result = new HashMap<>();
    for (Course course : courses) {
      String[] instructors = course.getInstructors().split(", ");
      for (String instructor : instructors) {
        if (!result.containsKey(instructor)) {
          result.put(instructor, new ArrayList<>(Arrays.asList(
                  new ArrayList<>(), new ArrayList<>())));
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
        for (String ss : list) {
          if (s.add(ss)) {
            l1.add(ss);
          }
        }
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
          } else {
            return -1;
          }
        }
      });
    } else if (by.equals("participants")) {
      sortedCourses.sort((a, b) -> {
        if (b.getParticipants() == (a.getParticipants())) {
          return a.title.compareTo(b.title);
        } else {
          if (b.participants > a.participants) {
            return 1;
          } else {
            return -1;
          }
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
    return result.subList(0, topK);

  }

  //5
  public List<String> searchCourses(String courseSubject,
                                    double percentAudited, double totalCourseHours) {
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
    Map<String, Double[]> courseAverages = new HashMap<>();
    for (Course course : courses) {
      String courseNumber = course.number;
      Double[] averages = courseAverages.getOrDefault(courseNumber, new Double[]{
        0.0, 0.0, 0.0, 0.0});
      averages[0] += course.medianAge;
      averages[1] += course.percentMale;
      averages[2] += course.percentDegree;
      averages[3]++;
      courseAverages.put(courseNumber, averages);
    }
    for (Map.Entry<String, Double[]> entry : courseAverages.entrySet()) {
      Double[] averages = entry.getValue();
      averages[0] /= averages[3];
      averages[1] /= averages[3];
      averages[2] /= averages[3];
    }

    // calculate similarity values
    Map<String, Double> similarityValues = new HashMap<>();
    for (Map.Entry<String, Double[]> entry : courseAverages.entrySet()) {
      double avgMedianAge = entry.getValue()[0];
      double avgMale = entry.getValue()[1];
      double avgBachelorOrHigher = entry.getValue()[2];
      double similarityValue = Math.pow(age - avgMedianAge, 2)
              + Math.pow(gender * 100 - avgMale, 2)
              + Math.pow(isBachelorOrHigher * 100 - avgBachelorOrHigher, 2);
      similarityValues.put(entry.getKey(), similarityValue);
    }
    List<Map.Entry<String, Double>> sortedSimilarityValues = new ArrayList<>(
            similarityValues.entrySet());
    sortedSimilarityValues.sort((a, b) -> {
      int result = a.getValue().compareTo(b.getValue());
      if (result == 0) {
        String titleA = courses.stream()
                .filter(row -> row.number.equals(a.getKey()))
                .max(Comparator.comparing(row -> row.launchDate))
                .get().title;
        String titleB = courses.stream()
                .filter(row -> row.number.equals(b.getKey()))
                .max(Comparator.comparing(row -> row.launchDate))
                .get().title;
        result = titleA.compareTo(titleB);
      }
      return result;
    });
    List<String> recommendedCourses = new ArrayList<>();
    for (Map.Entry<String, Double> sortedSimilarityValue : sortedSimilarityValues) {
      String courseNumber = sortedSimilarityValue.getKey();
      String courseTitle = courses.stream()
              .filter(row -> row.number.equals(courseNumber))
              .max(Comparator.comparing(row -> row.launchDate))
              .get().title;
      if (!recommendedCourses.contains(courseTitle)) {
        recommendedCourses.add(courseTitle);
      }
    }

    return recommendedCourses.subList(0, 10);
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
    if (title.startsWith("\"")) {
      title = title.substring(1);
    }
    if (title.endsWith("\"")) {
      title = title.substring(0, title.length() - 1);
    }
    this.title = title;
    if (instructors.startsWith("\"")) {
      instructors = instructors.substring(1);
    }
    if (instructors.endsWith("\"")) {
      instructors = instructors.substring(0, instructors.length() - 1);
    }
    this.instructors = instructors;
    if (subject.startsWith("\"")) {
      subject = subject.substring(1);
    }
    if (subject.endsWith("\"")) {
      subject = subject.substring(0, subject.length() - 1);
    }
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
