package ee.ttu.idk0071.sentiment.amqp;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.ttu.idk0071.sentiment.amqp.messages.DomainLookupRequestMessage;
import ee.ttu.idk0071.sentiment.lib.analysis.api.SentimentAnalyzer;
import ee.ttu.idk0071.sentiment.lib.analysis.api.SentimentRetrievalException;
import ee.ttu.idk0071.sentiment.lib.analysis.impl.ViveknSentimentAnalyzer;
import ee.ttu.idk0071.sentiment.lib.searching.api.Fetcher;
import ee.ttu.idk0071.sentiment.lib.searching.impl.GoogleFetcher;
import ee.ttu.idk0071.sentiment.lib.searching.objects.FetchException;
import ee.ttu.idk0071.sentiment.lib.searching.objects.Query;
import ee.ttu.idk0071.sentiment.model.Domain;
import ee.ttu.idk0071.sentiment.model.DomainLookup;
import ee.ttu.idk0071.sentiment.model.Lookup;
import ee.ttu.idk0071.sentiment.model.LookupEntity;
import ee.ttu.idk0071.sentiment.repository.DomainLookupRepository;
import ee.ttu.idk0071.sentiment.repository.DomainLookupStateRepository;

@Component
public class DomainLookupExecutor {
	@Autowired
	private DomainLookupStateRepository lookupStateRepository;
	@Autowired
	private DomainLookupRepository domainLookupRepository;

	@Transactional
	public void handleMessage(DomainLookupRequestMessage lookupRequest) throws FetchException {
		DomainLookup domainLookup = domainLookupRepository.findOne(lookupRequest.getDomainLookupId());
		Lookup lookup = domainLookup.getLookup();
		LookupEntity lookupEntity = lookup.getLookupEntity();
		String queryString = lookupEntity.getName();
		
		domainLookup.setDomainLookupState(lookupStateRepository.findByName("In progress"));
		domainLookupRepository.save(domainLookup);
		
		Domain domain = domainLookup.getDomain();
		long neutralCnt = 0, positiveCnt = 0, negativeCnt = 0;
		
		if ("Google".equals(domain.getName())) {
			Fetcher searcher = new GoogleFetcher();
			Query query = new Query(queryString, 10L);
			List<String> searchResults = searcher.fetch(query);
			
			SentimentAnalyzer analyzer = new ViveknSentimentAnalyzer();
			
			for (String text : searchResults) {
				try {
					switch (analyzer.getSentiment(text)) {
						case NEUTRAL:
							neutralCnt++;
							break;
						case POSITIVE:
							positiveCnt++;
							break;
						case NEGATIVE:
							negativeCnt++;
							break;
						default:
							break;
					}
				} catch (SentimentRetrievalException e) {
					continue;
				}
			}
		}
		
		domainLookup.setDomainLookupState(lookupStateRepository.findByName("Complete"));
		domainLookup.setCounts(negativeCnt, neutralCnt, positiveCnt);
		domainLookupRepository.save(domainLookup);
	}
}
