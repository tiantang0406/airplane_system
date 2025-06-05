import java.util.List;

public class TestFlightDates {
    public static void main(String[] args) {
        System.out.println("=== 测试FlightQueryModule的getAvailableDates方法 ===");
        
        try {
            // 获取可用日期列表
            List<String> availableDates = FlightQueryModule.DatabaseManager.getAvailableDates();
            
            System.out.println("找到的可用航班日期数量: " + availableDates.size());
            System.out.println("\n可用航班日期列表:");
            for (int i = 0; i < availableDates.size(); i++) {
                System.out.println((i + 1) + ". " + availableDates.get(i));
            }
            
            if (availableDates.isEmpty()) {
                System.out.println("\n警告: 没有找到任何可用的航班日期！");
                System.out.println("这可能是因为：");
                System.out.println("1. 数据库中没有航班数据");
                System.out.println("2. 所有航班日期都不在未来30天内");
                System.out.println("3. 数据库连接问题");
            }
            
            // 测试查询特定日期的航班
            if (!availableDates.isEmpty()) {
                String testDate = availableDates.get(0);
                System.out.println("\n=== 测试查询 " + testDate + " 的航班 ===");
                
                List<FlightQueryModule.FlightInfo> flights = 
                    FlightQueryModule.DatabaseManager.searchFlights("PEK", "SHA", testDate);
                    
                System.out.println("找到航班数量: " + flights.size());
                for (FlightQueryModule.FlightInfo flight : flights) {
                    System.out.println("航班: " + flight.flightNumber + 
                                     " " + flight.departureAirport + "->" + flight.arrivalAirport + 
                                     " " + flight.departureTime);
                }
            }
            
        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
