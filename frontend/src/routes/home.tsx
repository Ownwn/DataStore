import {useEffect, useState} from "react";
import styles from "./home.module.css";
import {DOMAIN} from "../App.tsx";

type Entry = { content: string, created_at: number }

export function Home() {
    let [items, setItems] = useState<Entry[]>([]);
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
        <InputForm postItem={postItem}/>
        <h2>Last Update: {secondsSinceUpdate + "s"}</h2>
        <h2>{error}</h2>
        <br/>
        <ol>
            {items.map((item: Entry, index) => (
                <li className={styles.dataItem} key={index}>
                    {formatEntry(item.content)}
                </li>
            ))}
        </ol>
    </div>;

    function formatEntry(entry: string)  {
        return (
            <>
                {entry.length >= 30 ? (
                    <details>
                        <summary>{entry.substring(0, 30) + "..."}</summary>
                        <p>{entry}</p>
                    </details>
                ) : entry}
                <button onClick={() => copyToClipboard(entry)} className={styles.dataButton}>Copy</button>

            </>)
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
        fetch(DOMAIN + "entries")
            .then(res => res.json())
            .then(data => data.sort((a: Entry, b: Entry) => b.created_at - a.created_at))
            .then(data => setItems(data))
            .then(_ => setSecondsSinceUpdate(0))
            .catch(err => console.log(err));
    }

    // @ts-ignore
    async function postItem(formData) {
        if (!formData) {
            return;
        }
        const text = formData.get("data");
        if (!text) {
            return;
        }


        const postResponse = await fetch(DOMAIN + "submit", {
            method: "POST",
            body: text
        });
        if (!postResponse.ok) {
            setError(await postResponse.text() + postResponse.status);
            return;
        }

        fetchItems();
    }
}

// @ts-ignore
function InputForm({postItem}) {
    return <form action={postItem}>
        <button type="submit">Submit</button>
        <br/>
        <textarea name="data" required/>
    </form>;
}