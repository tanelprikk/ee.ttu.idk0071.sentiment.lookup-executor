package ee.ttu.idk0071.sentiment.executor;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.ttu.idk0071.sentiment.lib.analysis.api.SentimentAnalyzer;
import ee.ttu.idk0071.sentiment.lib.analysis.api.SentimentRetrievalException;
import ee.ttu.idk0071.sentiment.lib.analysis.impl.ViveknSentimentAnalyzer;
import ee.ttu.idk0071.sentiment.lib.fetching.api.Fetcher;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.FetchException;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.Query;
import ee.ttu.idk0071.sentiment.lib.messages.DomainLookupRequestMessage;
import ee.ttu.idk0071.sentiment.model.Domain;
import ee.ttu.idk0071.sentiment.model.DomainLookup;
import ee.ttu.idk0071.sentiment.model.Lookup;
import ee.ttu.idk0071.sentiment.model.LookupEntity;
import ee.ttu.idk0071.sentiment.repository.DomainLookupRepository;
import ee.ttu.idk0071.sentiment.repository.DomainLookupStateRepository;
import ee.ttu.idk0071.sentiment.utils.FetcherFactory;

@Component
public class DomainLookupExecutor {
	private static final long MAX_QUERY_RESULTS = 1000L;

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
		Fetcher fetcher = FetcherFactory.getFetcher(domain);
		
		long neutralCnt = 0, 
			positiveCnt = 0, 
			negativeCnt = 0;
		
		if (fetcher != null) {
			
			Query query = new Query(queryString, MAX_QUERY_RESULTS);
			List<String> searchResults = fetcher.fetch(query);
			
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
			
		} else {
			domainLookup.setDomainLookupState(lookupStateRepository.findByName("Error"));
			return;
		}
		
		domainLookup.setDomainLookupState(lookupStateRepository.findByName("Complete"));
		domainLookup.setCounts(negativeCnt, neutralCnt, positiveCnt);
		domainLookupRepository.save(domainLookup);
	}
}
