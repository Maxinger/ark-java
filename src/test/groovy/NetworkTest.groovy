/*
 * This Spock specification was auto generated by running 'gradle init --type groovy-library'
 */


import spock.lang.Shared
import spock.lang.Specification
import io.ark.core.*
import spock.lang.Timeout

import java.util.concurrent.TimeUnit

class NetworkTest extends Specification {

    @Shared mainnet = Network.Mainnet
    @Shared devnet = Network.Devnet

    def setupSpec()
    {
        mainnet.warmup()
        devnet.warmup()
    }

    //Fails if any peer is unreachable.
    //Fails if peer lookup takes too long
    //Fails if any peer doesnt report back success status
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    def "All peers should be reachable"()
    {
        setup:
        boolean success = true

        when:
        (mainnet.peers + devnet.peers).each { peer ->
            success = success && peer.getStatus().get("success") as boolean
        }

        then:
        //Fail fast if an exception is thrown
        noExceptionThrown()
        success
    }

    def "Should connect to Mainnet"(){
      setup:
        def peer = mainnet.randomPeer
      when:
        def status = peer.getStatus()
      then:
        status.currentSlot > status.height
    }

    def "Should connect to Devnet"(){
      setup:
        def peer = devnet.randomPeer
      when:
        def status = peer.getStatus()
      then:
        status.currentSlot > status.height
    }

    def "Should post a transaction to a Mainnet Peer"(){
      setup:
        def peer = mainnet.randomPeer
      when:
        def transaction = Transaction.createTransaction("AXoXnFi4z1Z6aFvjEYkDVCtBGW2PaRiM25", 133380000000, "This is first transaction from JAVA", "this is a top secret passphrase")
        def result = peer << transaction
      then:
        result.error == "Account does not have enough ARK: AGeYmgbg2LgGxRW2vNNJvQ88PknEJsYizC balance: 0"
    }

    def "Should broadcast a transaction to Mainnet"(){
      when:
        def transaction = Transaction.createTransaction("AXoXnFi4z1Z6aFvjEYkDVCtBGW2PaRiM25", 133380000000, "This is first transaction from JAVA", "this is a top secret passphrase")
        def result = mainnet << transaction
      then:
        result == mainnet.broadcastMax
    }

    def "Should Get transactions associated with an Account"(){
        setup:
        def peer = mainnet.randomPeer
        when:
        def account = Account.newInstance([address:'AXoXnFi4z1Z6aFvjEYkDVCtBGW2PaRiM25'])
        def result = peer.getTransactions(account, 2)
        then:
        result.get("success") == true
        result.get("count").equals("2") //Count doesnt include filtering from api call
        (result.get("transactions") as List).size() == 2
    }

    def "Should only return one transaction from an Account"(){
        setup:
        def peer = mainnet.randomPeer
        when:
        def account = Account.newInstance([address:'AXoXnFi4z1Z6aFvjEYkDVCtBGW2PaRiM25'])
        def result = peer.getTransactions(account, 1)
        then:
        result.get("success") == true
        result.get("count").equals("2") //Count doesnt include filtering from api call
        (result.get("transactions") as List).size() == 1
    }

    def "Should be able to fetch a list of peers"(){
        setup:
        def peer = mainnet.randomPeer
        when:
        def result = peer.getPeers()
        then:
        result.get("success") == true
        (result.get("peers") as List).size() > 0
    }

    def "Should be able to fetch a list of delegates"(){
        setup:
        def peer = mainnet.randomPeer
        when:
        def result = peer.getDelegates()
        then:
        result.get("success") == true
        (result.get("totalCount") as int) > 0
        (result.get("delegates") as List).size() > 0
    }

}
