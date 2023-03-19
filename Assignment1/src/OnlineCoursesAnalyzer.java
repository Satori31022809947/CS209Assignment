package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 *
 * This is just a demo for you, please run it on JDK17 (some statements may be not allowed in lower version).
 * This is just a demo, and you can extend and implement functions
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

    public static <T> void forEach(List<T> list, Consumer<T> c) {
        for (T s : list) {
            c.accept(s);
        }
    }
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
        Map<String, Integer> PtcpCountByInst=new HashMap<>();
        forEach(courses, (Course s) -> PtcpCountByInst.put(s.institution,0));
        forEach(courses, (Course s) -> PtcpCountByInst.put(s.institution,PtcpCountByInst.get(s.institution)+s.participants));
        Map<String, Integer> result = PtcpCountByInst.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        return result;
    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> PtcpCountByInstAndSubject=new HashMap<>();
        forEach(courses, (Course s) -> PtcpCountByInstAndSubject.put(s.institution+'-'+s.subject,0));
        forEach(courses, (Course s) -> PtcpCountByInstAndSubject.put(s.institution+'-'+s.subject,PtcpCountByInstAndSubject.get(s.institution+'-'+s.subject)+s.participants));
        Map<String, Integer> result = PtcpCountByInstAndSubject.entrySet().stream()
                .sorted(Map.Entry.<String,Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        return result;
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Map<String, List<List<String>>>CourseListOfInstructor=new HashMap<>();
        forEach(courses, (Course s) ->{
            List<String> instructors= List.of(s.instructors.split(", "));
            instructors= instructors.stream().collect(Collectors.toSet()).stream().toList();
            for (String instructor:instructors) {
                if (CourseListOfInstructor.get(instructor)==null){
                    List<List<String>> lists=new ArrayList<>();
                    lists.add(new ArrayList<>());
                    lists.add(new ArrayList<>());
                    CourseListOfInstructor.put(instructor,lists);
                }
                if (instructors.size()==1){
                    CourseListOfInstructor.get(instructor).get(0).add(s.title);
                }
                else {
                    CourseListOfInstructor.get(instructor).get(1).add(s.title);
                }
            }
        });

        Map<String, List<List<String>>>result=new HashMap<>();
        CourseListOfInstructor.forEach((key,value)-> {
            List<String>list1=new ArrayList<>(new HashSet<>(value.get(0))),
                    list2=new ArrayList<>(new HashSet<>(value.get(1)));
            list1.sort(String::compareTo);
            list2.sort(String::compareTo);

            List<List<String>> lists=new ArrayList<>();
            lists.add(list1);
            lists.add(list2);
            result.put(key,lists);
        });
        return result;
    }

    //4
    public List<String> getCourses(int topK, String by) {
        if (by.equals("hours")) {
            courses=courses.stream().sorted(Comparator.comparingDouble(Course::getTotalHours).reversed()).collect(Collectors.toList());
        }
        if (by.equals("participants")){
            courses=courses.stream().sorted(Comparator.comparingDouble(Course::getParticipants).reversed()).collect(Collectors.toList());
        }
        List<String>result=new ArrayList<>();
        Set<String>stringSet=new HashSet<>();
        for (int i=0,count=0;count<topK;i++){
            if (stringSet.contains(courses.get(i).title))continue;
            stringSet.add(courses.get(i).title);
            result.add(courses.get(i).title);
            count++;
        }
        return result;
    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        courseSubject=courseSubject.toLowerCase();
        String finalCourseSubject = courseSubject;
        List<String>result=new ArrayList<>();
        Set<String>stringSet=new HashSet<>();
        forEach(courses, (Course s) -> {
            if (s.subject.toLowerCase().contains(finalCourseSubject)&&s.percentAudited>=percentAudited&&s.totalHours<=totalCourseHours){
                if (!stringSet.contains(s.title)) {
                    stringSet.add(s.title);
                }
            }
        });
        result=stringSet.stream().sorted(String::compareTo).toList();
        return result;
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        Map<String,RecommendCourse>recommendCourses=new HashMap<>();
        forEach(courses, (Course s) -> {
            if (recommendCourses.get(s.number)==null){
                recommendCourses.put(s.number,new RecommendCourse(s.title,s.launchDate));
            }
            recommendCourses.get(s.number).add(s.title,s.launchDate,s.medianAge,s.percentMale,s.percentDegree);
        });
        List<RecommendCourse> recommendCourseList=new ArrayList<>();
        List<RecommendCourse> finalRecommendCourseList = recommendCourseList;
        recommendCourses.forEach((key, value)-> {
            value.calcSimilarityValue(age,gender,isBachelorOrHigher);
            finalRecommendCourseList.add(value);
            System.out.println(value.getSimilarityValue() +" "+value.getTitle());
        });
        recommendCourseList=finalRecommendCourseList.stream().sorted(Comparator.comparingDouble(RecommendCourse::getSimilarityValue).
                thenComparing(RecommendCourse::getTitle)).toList();
        List<String>result=new ArrayList<>();
        Set<String>stringSet=new HashSet<>();
        for (int i=0,count=0;count<10&&i<recommendCourseList.size();i++){
            if (!stringSet.contains(recommendCourseList.get(i).getTitle())){
                count++;
                result.add(recommendCourseList.get(i).getTitle());
                stringSet.add(recommendCourseList.get(i).getTitle());
            }
        }
        return result;
    }

}
class RecommendCourse{
    private String title;
    private Date launchDate;
    private int count;
    private double averageMedianAge,averageMale,averageBachelor;
    private double similarityValue;
    public RecommendCourse(String title,Date launchDate){
        this.launchDate=launchDate;
        this.title=title;
        this.count=0;
        this.averageMedianAge=0;
        this.averageMale=0;
        this.averageBachelor=0;
        this.similarityValue=0;
    }
    public void add(String title,Date launchDate,double medianAge,double averageMale,double averageBachelor){
        if (launchDate.after(this.launchDate)){
            this.launchDate=launchDate;
            this.title=title;
        }
        this.averageMedianAge=(this.averageMedianAge*this.count+medianAge)/(this.count+1);
        this.averageMale=(this.averageMale*this.count+averageMale)/(this.count+1);
        this.averageBachelor=(this.averageBachelor*this.count+averageBachelor)/(this.count+1);
        this.count++;
    }
    public void calcSimilarityValue(int age,int gender,int isBachelorOrHigher){
        this.similarityValue=Math.pow((double) age-this.averageMedianAge,2)+
                Math.pow(gender*100-this.averageMale,2)+
                Math.pow(isBachelorOrHigher*100-this.averageBachelor,2);
    }

    public double getSimilarityValue(){
        return this.similarityValue;
    }
    public String getTitle(){
        return this.title;
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
    public double getTotalHours(){
        return totalHours;
    }
    public int getParticipants(){
        return participants;
    }
}