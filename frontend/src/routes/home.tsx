import {useEffect, useState} from "react";
import styles from "./home.module.css";

type Entry = { name: string, plainText: boolean, content: string, createdAt: number }

export function Home() {
    let [items, setItems] = useState<Entry[]>([]);
    // @ts-ignore
    let [error, setError] = useState("");
    let [secondsSinceUpdate, setSecondsSinceUpdate] = useState(0)

    useEffect(() => {
        fetchItems();

        const interval = setInterval(() => {
            setSecondsSinceUpdate(prev => prev + 1);
        }, 1000);

        return () => clearInterval(interval)
    }, []);


    return <div className={styles.dataBackground + " " + styles.main}>
        <GreetingForm fetchItems={fetchItems} setError={setError}/>
        <h2>Last Update: {secondsSinceUpdate + "s"}</h2>
        <h2>{error}</h2>
        <br/>
        <ol>
            {items.map((item: Entry, index) => (
                <li className={styles.dataItem} key={index}>
                    {formatEntry(item)}
                </li>
            ))}
        </ol>
    </div>;

    function formatEntry(entry: Entry)  {
        if (entry.plainText) {
            return (
                <>
                    {entry.content.length >= 30 ? (
                        <details>
                            <summary>{entry.content.substring(0, 30) + "..."}</summary>
                            <p>{entry.content}</p>
                        </details>
                    ) : entry.content}
                    <button onClick={() => copyToClipboard(entry.content)} className={styles.dataButton}>Copy</button>

                </>)
        }
        return <a href={"downloadfile/" + String(entry.createdAt)} download={entry.name}>{entry.name}</a>

    }

    async function copyToClipboard(text: string) {
        if (navigator.clipboard && navigator.clipboard.writeText) {
            await navigator.clipboard.writeText(text);
        } else { // thanks safari for not playing nice!!
            const textarea = document.createElement('textarea');
            textarea.value = text;
            document.body.appendChild(textarea);
            textarea.select();
            document.execCommand('copy');
            document.body.removeChild(textarea);
        }

    }


    function fetchItems() {
        fetch("entries")
            .then(res => res.json())
            .then(data => !data ? [{}] : data) // [{}] js makes me laugh sometimes
            .then(data => data.sort((a: Entry, b: Entry) => b.createdAt - a.createdAt))
            .then(data => setItems(data))
            .then(_ => setSecondsSinceUpdate(0))
            .catch(err => console.log(err));
    }
}


// @ts-ignore
function GreetingForm({ fetchItems, setError}) {
    const handleSubmit = async (e: any) => {
        e.preventDefault();
        const formData = new FormData(e.target);

        const files = formData.getAll("files")
        const text = formData.get("data")
        // @ts-ignore
        if (files[0].size === 0 && !text) {
            return;
        }

        if (text) {
            const textResponse = await fetch("submit", {
                method: "POST",
                headers: {
                    "Content-Type": "text/plain"
                },
                body: text
            });

            const result = await textResponse.text();
            console.log(result);
        }
        // @ts-ignore
        if (files[0].size !== 0) {
            const response = await fetch('submitfile', {
                method: 'POST',
                body: formData
            });



            if (!response.ok) {
                console.log("not ok " + response.status)
                setError(await response.text() + response.status);
                return;
            }

            const result = await response.text();
            console.log(result);
        }
        e.target.reset()
        fetchItems();
    };

    return (
        <form onSubmit={handleSubmit}>
            <textarea name="data" placeholder="Data to upload"/>
            <br/>
            <input type="file" multiple name="files" className={styles.fileInput}/>
            <br/>
            <button type="submit">Submit</button>
        </form>
    );
}