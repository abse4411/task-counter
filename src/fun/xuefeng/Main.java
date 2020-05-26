package fun.xuefeng;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Main {
    static class MetaData{
        //成员数量、已冲刺天数
        int memberCount, dayCount;
        //成员姓名
        String[] memName;
        //每个成员总工作量，所有成员当日累计工作量
        double[] memTotPoint, dayAccumPoint;
        //总工作量=Sum(每个成员总工作量)
        double sumPoint=0;
        //冲刺开始日期
        LocalDate startDate;
        //成员当日工作量，成员当日累计工作量
        double[][] memDayPoint, menDayAccumPoint;
        //所有成员当日的昨天完成的任务、昨天花了多少时间、还剩余多少时间、遇到什么困难、今天解决的进度、明天的计划
        Map<Integer,String[][]> map;
    }

    public static void main(String[] args) throws IOException {
        MetaData data = new MetaData();
        String[] strs;
        try(BufferedReader reader=new BufferedReader(new FileReader("resource/data.in"))){
            strs = reader.readLine().split(" ");
            data.memberCount=Integer.parseInt(strs[0]);
            data.startDate=LocalDate.parse(strs[1]);
            data.memName=new String[data.memberCount];
            data.memTotPoint=new double[data.memberCount];
            for (int i = 0; i < data.memberCount; i++) {
                strs = reader.readLine().split(" ");
                data.memName[i]=strs[0];
                data.memTotPoint[i]=Double.parseDouble(strs[1]);
                //data.sumPoint+=data.memTotPoint[i];
            }
            data.dayCount =Integer.parseInt(reader.readLine());
            data.memDayPoint =new double[data.dayCount][data.memberCount];
            data.dayAccumPoint =new double[data.dayCount];
            data.menDayAccumPoint =new double[data.dayCount][data.memberCount];
            data.map=new HashMap<>(257);
            double preDayAccumPoint=0;
            double[] preMemDayAccumPoint=new double[data.memberCount];
            for (int i = 0; i < data.dayCount; i++) {
                String[][] record=new String[data.memberCount][];
                for (int j = 0; j < data.memberCount; j++) {
                    record[j]=reader.readLine().split("#");
                    data.memDayPoint[i][j]=Double.parseDouble(record[j][1]);
                    data.sumPoint+=data.memDayPoint[i][j];
                    data.menDayAccumPoint[i][j]=preMemDayAccumPoint[j]+data.memDayPoint[i][j];
                    preDayAccumPoint+=data.memDayPoint[i][j];
                    preMemDayAccumPoint[j]=data.menDayAccumPoint[i][j];
                }
                reader.readLine();
                data.map.put(i, record);
                data.dayAccumPoint[i]=preDayAccumPoint;
                preDayAccumPoint=data.dayAccumPoint[i];
            }
        }
        System.out.println(generateBeta冲刺博客(data));
//        for (int i = 0; i < data.dayCount; i++) {
//            for (int j = 0; j < data.memberCount; j++) {
//                System.out.print(data.menDayAccumPoint[i][j]+"  ");
//            }
//            System.out.println();
//        }
    }

    private static String generateBeta冲刺博客(MetaData m){
        StringBuffer sb=new StringBuffer(2048);
        sb.append("##1. SCRUM会议");
        newLine(sb);
        generate会议记录表(m,sb);
        sb.append("####会议照片");
        newLine(sb);
        sb.append("####Commit记录");
        newLine(sb);
        sb.append("####Issue链接");
        newLine(sb);
        sb.append("##2. PM报告");
        newLine(sb);
        generateBeta工作量表(m,sb,7);
        sb.append("####燃尽图 - 以工作量（小时）为单位");
        newLine(sb);
        sb.append("####燃尽图 - 以任务卡片数量为单位");
        newLine(sb);
        sb.append("####任务总量变化线");
        newLine(sb);
        generateBeta成员贡献比表(m,sb);
        sb.append("####项目最新运行截图");
        newLine(sb);
        return sb.toString();
    }

    private static void generate会议记录表(MetaData m,StringBuffer sb){
        sb.append(format("####会议记录表(%s)",m.startDate.plusDays(m.dayCount-1)));
        newLine(sb);
        sb.append("组员|昨天完成的任务|今天花了多少时间|还剩余多少时间|遇到什么困难|今天解决的进度|明天的计划");
        newLine(sb);
        sb.append(":--|:--|:--|:--|:--|:--|:--");
        newLine(sb);
        String[][] record = m.map.get(m.dayCount - 1);
        for (int i = 0; i < m.memberCount; i++) {
            sb.append(format("%s|%s|%.1f|%s|%s|%s|%s",
                m.memName[i],
                record[i][0],
                m.memDayPoint[m.dayCount -1][i],
                record[i][2],
                record[i][3],
                record[i][4],
                record[i][5]));
            newLine(sb);
        }
    }

    private static void generateBeta工作量表(MetaData m,StringBuffer sb,int rows){
        sb.append("日期|整个项目预期工作量|目前已花的时间|剩余时间");
        newLine(sb);
        sb.append(":--|:--|:--|:--");
        newLine(sb);
        LocalDate curDate=m.startDate;
        for (int i = 0; i < rows; i++) {
            if(i<m.dayCount){
                sb.append(format("Beta冲刺(%d/%d) %d-%d|%.2f|%.2f|%.2f",
                    i+1,
                    rows,
                    curDate.getMonthValue(),
                    curDate.getDayOfMonth(),
                    m.sumPoint,
                    m.dayAccumPoint[i],
                    m.sumPoint-m.dayAccumPoint[i]));
            } else {
                sb.append(format("Beta冲刺(%d/%d) %d-%d|||",
                    i+1,
                    rows,
                    curDate.getMonthValue(),
                    curDate.getDayOfMonth()));
            }
            newLine(sb);
            curDate = curDate.plusDays(1);
        }
    }

    private static void generateBeta成员贡献比表(MetaData m,StringBuffer sb){
        sb.append("####成员贡献比");
        newLine(sb);
        sb.append("组员|今天完成的任务量（小时）|累计完成的任务量（小时）|今天贡献比|累计贡献比");
        newLine(sb);
        sb.append(":--|:--|:--|:--|:--");
        newLine(sb);
        double curDayTotPoint;
        if(m.dayCount-1==0){
            curDayTotPoint=m.dayAccumPoint[m.dayCount-1];
        }else{
            curDayTotPoint=m.dayAccumPoint[m.dayCount-1]-m.dayAccumPoint[m.dayCount-2];
        }
        for (int i = 0; i < m.memberCount; i++) {
            sb.append(format("%s|%.1f|%.1f|%2.2f%%|%2.2f%%",
                m.memName[i],
                m.memDayPoint[m.dayCount -1][i],
                m.menDayAccumPoint[m.dayCount -1][i],
                100*m.memDayPoint[m.dayCount -1][i]/curDayTotPoint,
                100*m.menDayAccumPoint[m.dayCount -1][i]/m.sumPoint));
            newLine(sb);
        }
    }

    private static void newLine(StringBuffer sb){
        sb.append(System.getProperty("line.separator"));
    }

    private static String format(String format,Object... arg){
        return String.format(format, arg);
    }
}
