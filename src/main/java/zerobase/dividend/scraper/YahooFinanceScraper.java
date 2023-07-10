package zerobase.dividend.scraper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.Dividend;
import zerobase.dividend.model.ScrapedResult;
import zerobase.dividend.model.constants.Month;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper{


    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";  //포맷형식으로 만들어주기
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";


    private static final long START_TIME = 86400; // 1일 60초 * 60분 * 24시간


    @Override
    public ScrapedResult scrap(Company company){

        var scarpResult = new ScrapedResult(); //var은 java10~ (타입 명시하지 않고 자동으로 추론 가능)
        scarpResult.setCompany(company);

      // html 코드를 가져올 url과 연결 -> 커넥션 객체 반환
        try {
            //시작날짜는 어떤 회사를 스크래핑하더라도 바꿀 필요가 없기때문에 상수로 사용
            //끝 날짜는 현재시간을 가져온 값(ms) 을 사용 (1970/01/01이후로 경과한 시간 ms)
            long now = System.currentTimeMillis() / 1000;

            String url = String.format(STATISTICS_URL ,company.getTicker(),START_TIME,now);
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();  //get() : 파싱한 결과를 리턴

            Elements parsingDivs = document.getElementsByAttributeValue("data-test", "historical-prices");//06:47
            Element tableEle = parsingDivs.get(0);  // table 전체

            Element tbody = tableEle.children().get(1);//thead ->  get(0), tfoot -> get(2)   //.children() -> 모든 child 속성의 Element를 가져옴?



            List<Dividend> dividends = new ArrayList<>();
            for(Element e : tbody.children()){
                String txt = e.text();
                if(!txt.endsWith("Dividend")){
                    continue;
                }

                String[] splits = txt.split(" ");
                int month = Month.strToNumber(splits[0]);
                int day = Integer.valueOf(splits[1].replace(",",""));
                int year = Integer.valueOf(splits[2].replace(",",""));
                String dividend = splits[3];

                //제대로 된 개월 값이 들어오지 않았을 때
                if(month < 0){
                    throw  new RuntimeException("Unexpected Month enum value ->" + splits[0]);
                }
                dividends.add( new Dividend(LocalDateTime.of(year,month,day,0,0),dividend)); //시간은 필요없으니 0으로 넣어주기

            }
            scarpResult.setDividends(dividends);


        } catch (IOException e) {
            // TODO
            throw new RuntimeException(e);
        }


        return  scarpResult;
    }




    @Override
    public Company scrapCompanyByTicker(String ticker){
        String url = String.format(SUMMARY_URL,ticker,ticker);

        try {
            Document document = Jsoup.connect(url).get();
            Element titleEle = document.getElementsByTag("h1").get(0);  // 강의에서는 주는 ticker은 0이 아닌 O였다.
            String title = titleEle.text().split(" - ")[1].trim();  // ex) "a - b - c" 인 경우 b를 반환

            return  new Company(ticker,title);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
