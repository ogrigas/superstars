package ogrigas.superstars.github

import spock.lang.Specification

class LinksSpec extends Specification {

    def "parses single link with rel metadata"() {
        when:
        def links = Links.parse('<http://test.com/>; rel="last"')
        then:
        links.rel('last') == Optional.of(new URL('http://test.com/'))
    }

    def "parses multiple links with rel metadata"() {
        when:
        def links = Links.parse('''
            <http://first>; rel="first",
            <http://last>; rel="last"
        ''')
        then:
        links.rel('first') == Optional.of(new URL('http://first'))
        links.rel('last') == Optional.of(new URL('http://last'))
    }

    def "ignores case"() {
        when:
        def links = Links.parse('<http://test.com/>; Rel="Last"')
        then:
        links.rel('LAST') == Optional.of(new URL('http://test.com/'))
    }

    def "ignores links without rel"() {
        when:
        def links = Links.parse('<http://first>; any="first", <http://last>; rel="last", <http://ignored>; rel=""')
        then:
        links.rel('first') == Optional.empty()
        links.rel('last') == Optional.of(new URL('http://last'))
        links.rel('') == Optional.empty()
    }

    def "ignores non-rel metadata"() {
        when:
        def links = Links.parse('<http://test.com/>; other="ignored"; rel="last"; ignored="other"')
        then:
        links.rel('last') == Optional.of(new URL('http://test.com/'))
        links.rel('ignored') == Optional.empty()
        links.rel('other') == Optional.empty()
    }

    def "ignores malformed url"() {
        when:
        def links = Links.parse('<//malformed>; rel="last"')
        then:
        links.rel('last') == Optional.empty()
    }

    def "handles empty and malformed headers"() {
        when:
        def links = Links.parse(header)
        then:
        links.rel('any') == Optional.empty()
        where:
        header << [null, '', ' ', ',', '<>', '<', ';']
    }
}
